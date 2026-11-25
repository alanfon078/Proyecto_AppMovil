const { createHash, randomUUID } = require('node:crypto');
const fsSync = require('node:fs');
const fs = require('node:fs/promises');
const path = require('node:path');
const multer = require('multer');
const { z } = require('zod');
const { pool, inTransaction } = require('./db');
const { bloquearAspirante, getAspirante } = require('./aspirantes');
const { HttpError, route, requireFound, idSchema } = require('./http');

const tiposObligatorios = ['ACTA_NACIMIENTO', 'CURP', 'CERTIFICADO_BACHILLERATO', 'COMPROBANTE_DOMICILIO'];
const observacionesEstados = ['rechazado', 'requiere_correccion', 'presencial'];
const maxPdfBytes = 5 * 1024 * 1024;
const uploadDir = path.resolve(__dirname, '..', process.env.UPLOAD_DIR || 'uploads');
const upload = multer({
  storage: multer.memoryStorage(),
  limits: { files: 1, fields: 2, parts: 4, fieldSize: 256, fileSize: maxPdfBytes + 1 }
}).single('archivo');

const seleccionarDocumentos =
  'SELECT d.id_documento, d.id_aspirante, d.id_tipo_documento, t.codigo AS tipo_codigo,' +
  ' d.nombre_original, d.tamano_bytes, d.tipo_mime, d.hash_sha256, d.version, d.fecha_subida, d.estado,' +
  ' (SELECT r.observaciones FROM revisiones r WHERE r.id_documento = d.id_documento' +
  ' ORDER BY r.id_revision DESC LIMIT 1) AS observaciones' +
  ' FROM documentos d JOIN tipos_documento t ON t.id_tipo_documento = d.id_tipo_documento';

async function initUploads() {
  await fs.mkdir(uploadDir, { recursive: true, mode: 0o700 });
  const counts = await getConteoDocumentos(pool, 0);
  if (counts.requeridos !== tiposObligatorios.length) {
    throw new Error('La base de datos debe tener los cuatro tipos de documento obligatorios del ITSUR.');
  }
}

async function getConteoDocumentos(connection, idAspirante) {
  const [rows] = await connection.execute(
    'SELECT COUNT(*) AS requeridos, COUNT(d.id_documento) AS subidos,' +
    " COUNT(CASE WHEN d.estado = 'aprobado' THEN 1 END) AS aprobados," +
    " COUNT(CASE WHEN d.estado = 'pendiente' THEN 1 END) AS pendientes," +
    ' COUNT(CASE WHEN d.estado IN (?, ?, ?) THEN 1 END) AS rechazados' +
    ' FROM tipos_documento t' +
    ' LEFT JOIN documentos d ON d.id_tipo_documento = t.id_tipo_documento AND d.id_aspirante = ? AND d.vigente = TRUE' +
    ' WHERE t.obligatorio = TRUE AND t.codigo IN (?, ?, ?, ?)',
    [...observacionesEstados, idAspirante, ...tiposObligatorios]
  );
  return rows[0];
}

async function recalcularEstadoAspirante(connection, idAspirante, estadoAnterior) {
  const { subidos, aprobados, rechazados } = await getConteoDocumentos(connection, idAspirante);
  let estado = 'en_revision';
  if (rechazados > 0) estado = 'con_observaciones';
  else if (subidos < tiposObligatorios.length) estado = 'en_captura';
  else if (estadoAnterior === 'documentacion_aprobada' && aprobados === tiposObligatorios.length) {
    return estadoAnterior;
  }
  await connection.execute('UPDATE aspirantes SET estado_inscripcion = ? WHERE id_aspirante = ?', [estado, idAspirante]);
  return estado;
}

async function listarDocumentos(idAspirante) {
  const [rows] = await pool.execute(
    seleccionarDocumentos + ' WHERE d.id_aspirante = ? AND d.vigente = TRUE ORDER BY t.orden, t.id_tipo_documento',
    [idAspirante]
  );
  return rows;
}

async function getDocumento(connection, id) {
  const [rows] = await connection.execute(seleccionarDocumentos + ' WHERE d.id_documento = ?', [id]);
  return rows[0];
}

function recibirArchivo(req, res, next) {
  upload(req, res, error => {
    if (!error) return next();
    if (error.code === 'LIMIT_FILE_SIZE') {
      return next(new HttpError(413, 'FILE_TOO_LARGE', 'El PDF debe pesar como máximo 5 MB'));
    }
    next(new HttpError(422, 'INVALID_MULTIPART', 'Envía un archivo PDF, su tipo y su hash SHA-256'));
  });
}

const getMisDocumentos = route(async (req, res) => {
  res.json(await listarDocumentos(req.user.idAspirante));
});

const getDocumentosAspirante = route(async (req, res) => {
  const id = idSchema.parse(req.params.id);
  await getAspirante(pool, id);
  res.json(await listarDocumentos(id));
});

function nombreOriginal(file) {
  // Busboy interpreta filename como latin1. Android envía los nombres en UTF-8.
  const utf8 = Buffer.from(file.originalname, 'latin1').toString('utf8');
  const nombre = utf8.includes('\uFFFD') ? file.originalname : utf8;
  return z.string().min(1).max(255).parse(path.win32.basename(nombre));
}

async function eliminarArchivo(ruta) {
  const archivo = path.resolve(uploadDir, ruta);
  if (path.dirname(archivo) !== uploadDir) {
    console.error('[PDF] La ruta almacenada está fuera del directorio de carga');
    return;
  }
  try {
    await fs.rm(archivo, { force: true });
  } catch (error) {
    console.error('[PDF] No se pudo eliminar el archivo:', error.code);
  }
}

const subirDocumento = route(async (req, res) => {
  const file = req.file;
  if (!file || file.mimetype !== 'application/pdf' ||
      !file.buffer.subarray(0, 5).equals(Buffer.from('%PDF-')) ||
      !file.buffer.subarray(-1024).includes(Buffer.from('%%EOF'))) {
    throw new HttpError(422, 'INVALID_PDF', 'Adjunta un archivo PDF válido');
  }
  const { tipoCodigo, hashSha256 } = z.object({
    tipoCodigo: z.enum(tiposObligatorios),
    hashSha256: z.string().regex(/^[a-f0-9]{64}$/i, 'Indica un hash SHA-256 válido')
  }).parse(req.body);
  const hashReal = createHash('sha256').update(file.buffer).digest('hex');
  if (hashReal !== hashSha256.toLowerCase()) {
    throw new HttpError(422, 'HASH_MISMATCH', 'El archivo recibido no coincide con su hash SHA-256');
  }
  const idAspirante = req.user.idAspirante;
  const ruta = randomUUID() + '.pdf';
  const nombre = nombreOriginal(file);
  let archivoCreado = false;
  let trabajoCompletado = false;
  try {
    const result = await inTransaction(async connection => {
      const aspirante = await bloquearAspirante(connection, idAspirante);
      const [tipos] = await connection.execute(
        'SELECT id_tipo_documento, max_bytes FROM tipos_documento WHERE codigo = ? AND obligatorio = TRUE',
        [tipoCodigo]
      );
      const tipo = requireFound(tipos[0], 'DOCUMENT_TYPE_NOT_FOUND', 'Tipo de documento no disponible');
      if (file.size > Math.min(tipo.max_bytes, maxPdfBytes)) {
        throw new HttpError(413, 'FILE_TOO_LARGE', 'El PDF supera el tamaño permitido para este documento');
      }
      const [vigentes] = await connection.execute(
        'SELECT id_documento, estado, hash_sha256 FROM documentos WHERE id_aspirante = ? AND id_tipo_documento = ? AND vigente = TRUE',
        [idAspirante, tipo.id_tipo_documento]
      );
      const vigente = vigentes[0];
      if (vigente?.hash_sha256 === hashReal) {
        return { documento: await getDocumento(connection, vigente.id_documento), estado_inscripcion: aspirante.estado_inscripcion };
      }
      if (vigente?.estado === 'aprobado') {
        throw new HttpError(409, 'DOCUMENT_ALREADY_APPROVED', 'No se puede sustituir un documento aprobado');
      }
      const [[versions]] = await connection.execute(
        'SELECT COALESCE(MAX(version), 0) AS ultima FROM documentos WHERE id_aspirante = ? AND id_tipo_documento = ?',
        [idAspirante, tipo.id_tipo_documento]
      );
      if (vigente) {
        await connection.execute('UPDATE documentos SET vigente = FALSE WHERE id_documento = ?', [vigente.id_documento]);
      }
      const [insert] = await connection.execute(
        'INSERT INTO documentos (id_aspirante, id_tipo_documento, nombre_original, ruta_archivo,' +
        ' tipo_mime, tamano_bytes, hash_sha256, version) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
        [idAspirante, tipo.id_tipo_documento, nombre, ruta, 'application/pdf', file.size, hashReal, versions.ultima + 1]
      );
      const estado = await recalcularEstadoAspirante(connection, idAspirante, aspirante.estado_inscripcion);
      const documento = await getDocumento(connection, insert.insertId);
      const handle = await fs.open(path.join(uploadDir, ruta), 'wx', 0o600);
      archivoCreado = true;
      try {
        await handle.writeFile(file.buffer);
      } finally {
        await handle.close();
      }
      trabajoCompletado = true;
      return { documento, estado_inscripcion: estado };
    });
    res.status(archivoCreado ? 201 : 200).json(result);
  } catch (error) {
    // Si falla la confirmación de MySQL, conservar el archivo evita perder un PDF posiblemente registrado.
    if (archivoCreado && !trabajoCompletado) await eliminarArchivo(ruta);
    throw error;
  }
});

const eliminarDocumento = route(async (req, res) => {
  const id = idSchema.parse(req.params.id);
  const idAspirante = req.user.idAspirante;
  const ruta = await inTransaction(async connection => {
    const aspirante = await bloquearAspirante(connection, idAspirante);
    const [rows] = await connection.execute(
      'SELECT estado, ruta_archivo FROM documentos WHERE id_documento = ? AND id_aspirante = ? AND vigente = TRUE',
      [id, idAspirante]
    );
    const doc = requireFound(rows[0], 'DOCUMENT_NOT_FOUND', 'Documento no encontrado');
    if (doc.estado === 'aprobado') {
      throw new HttpError(409, 'DOCUMENT_ALREADY_APPROVED', 'No se puede eliminar un documento aprobado');
    }
    await connection.execute('DELETE FROM documentos WHERE id_documento = ?', [id]);
    await recalcularEstadoAspirante(connection, idAspirante, aspirante.estado_inscripcion);
    return doc.ruta_archivo;
  });
  await eliminarArchivo(ruta);
  res.sendStatus(204);
});

const revisarDocumento = route(async (req, res) => {
  const id = idSchema.parse(req.params.id);
  const { resultado, observaciones } = z.object({
    resultado: z.enum(['aprobado', ...observacionesEstados]),
    observaciones: z.string().trim().max(2000).optional()
  }).parse(req.body);
  if (resultado !== 'aprobado' && !observaciones) {
    throw new HttpError(422, 'OBSERVACIONES_REQUIRED', 'Indica las observaciones del dictamen');
  }
  const [owners] = await pool.execute('SELECT id_aspirante FROM documentos WHERE id_documento = ?', [id]);
  const { id_aspirante: idAspirante } = requireFound(owners[0], 'DOCUMENT_NOT_FOUND', 'Documento no encontrado');
  const result = await inTransaction(async connection => {
    const aspirante = await bloquearAspirante(connection, idAspirante);
    const [presentes] = await connection.execute('SELECT vigente FROM documentos WHERE id_documento = ?', [id]);
    requireFound(presentes[0]?.vigente, 'DOCUMENT_NOT_FOUND', 'No se encontró una versión vigente del documento');
    const [revision] = await connection.execute(
      'INSERT INTO revisiones (id_documento, id_revisor, resultado, observaciones) VALUES (?, ?, ?, ?)',
      [id, req.user.idUsuario, resultado, observaciones || null]
    );
    await connection.execute('UPDATE documentos SET estado = ? WHERE id_documento = ?', [resultado, id]);
    const estado = await recalcularEstadoAspirante(connection, idAspirante, aspirante.estado_inscripcion);
    const documento = await getDocumento(connection, id);
    await connection.execute(
      "INSERT INTO notificaciones (id_usuario, id_revision, tipo, id_documento, titulo, mensaje) VALUES (?, ?, 'documento', ?, ?, ?)",
      [
        aspirante.id_usuario, revision.insertId, id, 'Dictamen de Control Escolar',
        observaciones || 'Control Escolar aprobó tu documento.'
      ]
    );
    return {
      id_revision: revision.insertId, id_documento: documento.id_documento, resultado,
      observaciones: documento.observaciones, estado_inscripcion: estado, documento
    };
  });
  res.json(result);
});

const aprobarTramite = route(async (req, res) => {
  const id = idSchema.parse(req.params.id);
  const result = await inTransaction(async connection => {
    const aspirante = await bloquearAspirante(connection, id);
    const { requeridos, aprobados } = await getConteoDocumentos(connection, id);
    if (requeridos !== tiposObligatorios.length || aprobados !== tiposObligatorios.length) {
      throw new HttpError(409, 'DOCS_INCOMPLETE', 'Control Escolar debe aprobar los cuatro documentos obligatorios');
    }
    if (aspirante.estado_inscripcion !== 'documentacion_aprobada') {
      await connection.execute(
        "UPDATE aspirantes SET estado_inscripcion = 'documentacion_aprobada' WHERE id_aspirante = ?", [id]
      );
      await connection.execute(
        "INSERT INTO notificaciones (id_usuario, tipo, titulo, mensaje) VALUES (?, 'sistema', ?, ?)",
        [aspirante.id_usuario, 'Documentos aprobados', 'Control Escolar aprobó la documentación de tu trámite.']
      );
    }
    return getAspirante(connection, id);
  });
  res.json(result);
});

const descargarArchivoDocumento = route(async (req, res) => {
  const id = idSchema.parse(req.params.id);
  const [rows] = await pool.execute(
    'SELECT id_documento, nombre_original, ruta_archivo, tipo_mime FROM documentos WHERE id_documento = ? AND vigente = TRUE',
    [id]
  );
  const doc = requireFound(rows[0], 'DOCUMENT_NOT_FOUND', 'Documento no encontrado');
  const archivo = path.resolve(uploadDir, doc.ruta_archivo);
  if (path.dirname(archivo) !== uploadDir) {
    throw new HttpError(404, 'FILE_NOT_FOUND', 'Archivo no disponible');
  }
  let stat;
  try {
    stat = await fs.stat(archivo);
  } catch {
    throw new HttpError(404, 'FILE_NOT_FOUND', 'El archivo PDF no se encuentra en el servidor');
  }
  if (!stat.isFile()) {
    throw new HttpError(404, 'FILE_NOT_FOUND', 'El archivo PDF no se encuentra en el servidor');
  }
  const nombreSeguro = (doc.nombre_original || `documento_${id}.pdf`).replace(/["\r\n\\]/g, '_');
  res.set({
    'Content-Type': 'application/pdf',
    'Content-Length': String(stat.size),
    'Content-Disposition': `inline; filename="${nombreSeguro}"`,
    'Cache-Control': 'private, no-store'
  });
  const stream = fsSync.createReadStream(archivo);
  stream.on('error', err => {
    if (!res.headersSent) {
      res.status(500).json({ error: { code: 'FILE_READ_ERROR', message: 'No se pudo leer el archivo PDF' } });
    } else {
      res.destroy(err);
    }
  });
  stream.pipe(res);
});

module.exports = {
  initUploads, getConteoDocumentos, recibirArchivo, getMisDocumentos, getDocumentosAspirante,
  subirDocumento, eliminarDocumento, revisarDocumento, aprobarTramite, descargarArchivoDocumento
};
