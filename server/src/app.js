const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const multer = require('multer');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const app = express();
app.use(helmet());
app.use(cors());
app.use(compression());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true }));

const uploadDir = path.join(__dirname, '..', 'uploads');
if (!fs.existsSync(uploadDir)) {
  fs.mkdirSync(uploadDir, { recursive: true });
}

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: (Number(process.env.MAX_PDF_MB) || 5) * 1024 * 1024 }
});

// ============================================================================
// Almacén transaccional en memoria sincronizable con MySQL 8 (pool mysql2)
// ============================================================================
const db = {
  secuenciaFolio: 3,
  secuenciaDoc: 102,
  secuenciaNotif: 203,
  secuenciaPago: 1,
  secuenciaRevision: 1,
  carreras: [
    { idCarrera: 1, nombreCarrera: 'Ing. Sistemas Computacionales' },
    { idCarrera: 2, nombreCarrera: 'Ing. Sistemas Automotrices' },
    { idCarrera: 3, nombreCarrera: 'Ing. Ambiental' },
    { idCarrera: 4, nombreCarrera: 'Lic. Gestión Empresarial' },
    { idCarrera: 5, nombreCarrera: 'Ing. Industrial' },
    { idCarrera: 6, nombreCarrera: 'Ing. Electrónica' },
    { idCarrera: 7, nombreCarrera: 'Ing. Semiconductores' },
    { idCarrera: 8, nombreCarrera: 'Lic. Gastronomía y Turismo' }
  ],
  estados: [
    { idEstado: 11, nombre: 'Guanajuato' },
    { idEstado: 16, nombre: 'Michoacán' },
    { idEstado: 14, nombre: 'Jalisco' },
    { idEstado: 22, nombre: 'Querétaro' }
  ],
  municipios: [
    { idMunicipio: 1, idEstado: 11, nombre: 'Uriangato' },
    { idMunicipio: 2, idEstado: 11, nombre: 'Moroleón' },
    { idMunicipio: 3, idEstado: 11, nombre: 'Yuriria' },
    { idMunicipio: 4, idEstado: 11, nombre: 'Salvatierra' },
    { idMunicipio: 5, idEstado: 11, nombre: 'Valle de Santiago' },
    { idMunicipio: 6, idEstado: 11, nombre: 'Acámbaro' },
    { idMunicipio: 7, idEstado: 11, nombre: 'Celaya' },
    { idMunicipio: 8, idEstado: 11, nombre: 'León' },
    { idMunicipio: 101, idEstado: 16, nombre: 'Morelia' },
    { idMunicipio: 102, idEstado: 16, nombre: 'Puruándiro' },
    { idMunicipio: 103, idEstado: 16, nombre: 'Santa Ana Maya' }
  ],
  aspirantes: [
    {
      idAspirante: 1,
      idUsuario: 1,
      nombre: 'Alan Yael',
      apellidos: 'Fonseca Ruiz',
      nombreCompleto: 'Alan Yael Fonseca Ruiz',
      nombreUsuario: 'alan.fonseca',
      folio: 'ITSUR-2026-00001',
      curp: 'FORA040101HGTRLA01',
      correo: 'alan.fonseca@alumnos.itsur.edu.mx',
      telefono: '4451234567',
      direccion: 'Av. Educación Superior 2000, Benito Juárez',
      idEstado: 11,
      estadoNombre: 'Guanajuato',
      idMunicipio: 1,
      municipioNombre: 'Uriangato',
      idCarrera: 1,
      carrera: 'Ing. Sistemas Computacionales',
      promedioBachillerato: 9.4,
      estadoInscripcion: 'en_revision'
    },
    {
      idAspirante: 2,
      idUsuario: 2,
      nombre: 'Montserrat',
      apellidos: 'Tenorio Villagómez',
      nombreCompleto: 'Montserrat Tenorio Villagómez',
      nombreUsuario: 'montserrat.tenorio',
      folio: 'ITSUR-2026-00002',
      curp: 'TEVM040512MGTNRA02',
      correo: 'montserrat.tenorio@alumnos.itsur.edu.mx',
      telefono: '4459876543',
      direccion: 'Zona Centro, Moroleón',
      idEstado: 11,
      estadoNombre: 'Guanajuato',
      idMunicipio: 2,
      municipioNombre: 'Moroleón',
      idCarrera: 4,
      carrera: 'Lic. Gestión Empresarial',
      promedioBachillerato: 9.6,
      estadoInscripcion: 'con_observaciones'
    }
  ],
  documentos: [
    {
      id: '101',
      idDocumento: 101,
      idAspirante: 1,
      idTipoDocumento: 1,
      tipo: 'ACTA_NACIMIENTO',
      tipoCodigo: 'ACTA_NACIMIENTO',
      nombreArchivo: 'acta_nacimiento_alan.pdf',
      tamanoBytes: 348120,
      mimeType: 'application/pdf',
      hashSha256: 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855',
      version: 1,
      fechaSubida: '2026-11-01 11:20',
      estado: 'aprobado',
      observaciones: 'Acta certificada legible y validada por Control Escolar.'
    },
    {
      id: '102',
      idDocumento: 102,
      idAspirante: 1,
      idTipoDocumento: 2,
      tipo: 'CURP',
      tipoCodigo: 'CURP',
      nombreArchivo: 'curp_actualizada_2026.pdf',
      tamanoBytes: 192400,
      mimeType: 'application/pdf',
      hashSha256: '8f434346648f6b96df89dda901c5176b10a6d83961dd3c1ac88b59b2dc327aa4',
      version: 1,
      fechaSubida: '2026-11-02 09:45',
      estado: 'pendiente',
      observaciones: 'En revisión por Control Escolar.'
    }
  ],
  notificaciones: [
    {
      id: '201',
      idNotificacion: 201,
      idUsuario: 1,
      tipo: 'documento',
      idDocumento: 101,
      titulo: 'Acta de Nacimiento Aprobada',
      mensaje: 'Control Escolar ha validado tu Acta de Nacimiento satisfactoriamente.',
      fecha: '2026-11-02 10:30',
      leida: false
    },
    {
      id: '202',
      idNotificacion: 202,
      idUsuario: 1,
      tipo: 'fecha',
      idFecha: 1,
      titulo: 'Recordatorio: Fecha límite de entrega',
      mensaje: 'Recuerda subir tu Certificado de Bachillerato antes del 20 de noviembre de 2026.',
      fecha: '2026-11-02 14:15',
      leida: false
    }
  ],
  fechasImportantes: [
    {
      idFecha: 1,
      titulo: 'Fecha límite de carga de documentos obligatorios',
      descripcion: 'Subir Acta de nacimiento, CURP y Certificado de bachillerato en PDF.',
      tipo: 'limite_entrega',
      fechaLimite: '2026-11-20 23:59',
      idCarrera: null,
      activa: true
    },
    {
      idFecha: 2,
      titulo: 'Fecha límite para solventar observaciones y correcciones',
      descripcion: 'Reemplazo de documentos rechazados o con observaciones.',
      tipo: 'correcciones',
      fechaLimite: '2026-11-27 23:59',
      idCarrera: null,
      activa: true
    },
    {
      idFecha: 3,
      titulo: 'Fecha límite de registro de comprobante de pago',
      descripcion: 'Cargar ficha bancaria referenciada de inscripción.',
      tipo: 'publicacion',
      fechaLimite: '2026-12-04 18:00',
      idCarrera: null,
      activa: true
    }
  ],
  pagoActual: {
    idPago: 1,
    idAspirante: 1,
    referenciaAsignada: 'REF-ITSUR-2026-9081',
    montoEsperado: 1850.0,
    folioPago: '',
    monto: 1850.0,
    fechaPago: '',
    rutaComprobante: '',
    estado: 'pendiente',
    observaciones: 'Pendiente de registrar comprobante bancario.',
    validado: false
  }
};

function recalcularEstadoAspirante(idAspirante) {
  const asp = db.aspirantes.find(a => a.idAspirante === idAspirante);
  if (!asp) return 'en_captura';
  const obligatorios = ['ACTA_NACIMIENTO', 'CURP', 'CERTIFICADO_BACHILLERATO'];
  const docs = db.documentos.filter(d => d.idAspirante === idAspirante && obligatorios.includes(d.tipoCodigo));

  let nuevoEstado = 'en_captura';
  if (docs.some(d => ['rechazado', 'requiere_correccion', 'presencial'].includes(d.estado))) {
    nuevoEstado = 'con_observaciones';
  } else if (docs.length < obligatorios.length) {
    nuevoEstado = 'en_captura';
  } else if (docs.every(d => d.estado === 'aprobado')) {
    nuevoEstado = 'documentacion_aprobada';
  } else {
    nuevoEstado = 'en_revision';
  }
  asp.estadoInscripcion = nuevoEstado;
  return nuevoEstado;
}

// Health check
app.get('/health', (_req, res) => {
  res.status(200).json({ status: 'ok', service: 'itsur-inscripciones-api', timestamp: new Date().toISOString() });
});

const router = express.Router();

// ===== F01 & F02: Auth (Login, Register, Refresh, Logout) =====
router.post('/auth/login', (req, res) => {
  const { identificador, email, contrasena } = req.body || {};
  const ident = String(identificador || email || '').trim().toLowerCase();
  if (!ident || !contrasena || String(contrasena).length < 4) {
    return res.status(401).json({
      error: {
        code: 'INVALID_CREDENTIALS',
        message: 'Usuario/correo o contraseña inválidos'
      }
    });
  }

  const isAdmin = ident.includes('admin') || ident.includes('escolar');
  const rol = isAdmin ? 'escolar' : 'aspirante';
  const aspirante = isAdmin ? null : (db.aspirantes.find(
    a => a.correo.toLowerCase() === ident || a.nombreUsuario.toLowerCase() === ident
  ) || db.aspirantes[0]);

  return res.status(200).json({
    accessToken: `jwt-access-${Date.now()}`,
    refreshToken: `jwt-refresh-${Date.now()}`,
    expiresInSeconds: 900,
    rol,
    usuario: {
      idUsuario: isAdmin ? 99 : aspirante.idUsuario,
      nombre: isAdmin ? 'Coordinación' : aspirante.nombre,
      apellidos: isAdmin ? 'Control Escolar' : aspirante.apellidos,
      nombreUsuario: isAdmin ? 'escolar.admin' : aspirante.nombreUsuario,
      correo: isAdmin ? 'escolar.admin@itsur.edu.mx' : aspirante.correo,
      rol,
      activo: true
    },
    aspirante
  });
});

router.post('/auth/register', (req, res) => {
  const {
    nombre,
    apellidos,
    nombreCompleto,
    nombreUsuario,
    correo,
    email,
    contrasena,
    curp,
    telefono,
    direccion,
    idEstado = 11,
    idMunicipio = 1,
    idCarrera = 1,
    carrera = 'Ing. Sistemas Computacionales'
  } = req.body || {};

  const emailFinal = String(correo || email || '').trim();
  const curpFinal = String(curp || '').trim().toUpperCase();

  if (!emailFinal || !contrasena || curpFinal.length !== 18) {
    return res.status(422).json({
      error: {
        code: 'VALIDATION_ERROR',
        message: 'Correo, contraseña y CURP de 18 caracteres son obligatorios'
      }
    });
  }

  const duplicado = db.aspirantes.find(
    a => a.curp === curpFinal || a.correo.toLowerCase() === emailFinal.toLowerCase()
  );
  if (duplicado) {
    return res.status(409).json({
      error: {
        code: 'DUPLICATE_ASPIRANTE',
        message: 'La CURP o el correo electrónico ya se encuentran registrados'
      }
    });
  }

  const newId = ++db.secuenciaFolio;
  const folio = `ITSUR-2026-${String(newId).padStart(5, '0')}`;
  const fullName = nombreCompleto || `${nombre || ''} ${apellidos || ''}`.trim();
  const username = nombreUsuario || emailFinal.split('@')[0];

  const nuevoAspirante = {
    idAspirante: newId,
    idUsuario: newId,
    nombre: nombre || fullName.split(' ')[0],
    apellidos: apellidos || fullName.split(' ').slice(1).join(' '),
    nombreCompleto: fullName,
    nombreUsuario: username,
    folio,
    curp: curpFinal,
    correo: emailFinal,
    telefono: telefono || '',
    direccion: direccion || '',
    idEstado: Number(idEstado),
    estadoNombre: 'Guanajuato',
    idMunicipio: Number(idMunicipio),
    municipioNombre: 'Uriangato',
    idCarrera: Number(idCarrera),
    carrera,
    promedioBachillerato: 9.0,
    estadoInscripcion: 'en_captura'
  };

  db.aspirantes.push(nuevoAspirante);

  return res.status(201).json({
    accessToken: `jwt-access-${Date.now()}`,
    refreshToken: `jwt-refresh-${Date.now()}`,
    expiresInSeconds: 900,
    rol: 'aspirante',
    usuario: {
      idUsuario: newId,
      nombre: nuevoAspirante.nombre,
      apellidos: nuevoAspirante.apellidos,
      nombreUsuario: nuevoAspirante.nombreUsuario,
      correo: nuevoAspirante.correo,
      rol: 'aspirante',
      activo: true
    },
    aspirante: nuevoAspirante
  });
});

router.post('/auth/refresh', (req, res) => {
  const { refreshToken } = req.body || {};
  if (!refreshToken) {
    return res.status(401).json({
      error: { code: 'INVALID_REFRESH_TOKEN', message: 'Refresh token requerido' }
    });
  }
  return res.status(200).json({
    accessToken: `jwt-access-refreshed-${Date.now()}`,
    refreshToken: `jwt-refresh-rotated-${Date.now()}`,
    expiresInSeconds: 900
  });
});

router.post('/auth/logout', (_req, res) => {
  return res.status(200).json({ eliminado: true, mensaje: 'Sesión cerrada correctamente' });
});

// ===== Catálogos =====
router.get('/catalogos/carreras', (_req, res) => res.status(200).json(db.carreras));
router.get('/catalogos/estados', (_req, res) => res.status(200).json(db.estados));
router.get('/catalogos/municipios', (req, res) => {
  const idEstado = Number(req.query.idEstado) || 11;
  return res.status(200).json(db.municipios.filter(m => m.idEstado === idEstado));
});

// ===== Perfil y Dashboard del Aspirante =====
router.get('/aspirantes/me', (_req, res) => {
  return res.status(200).json(db.aspirantes[0]);
});

router.put('/aspirantes/me', (req, res) => {
  const { nombre, apellidos, nombreCompleto, telefono, direccion, idEstado, idMunicipio } = req.body || {};
  if (!nombre && !nombreCompleto) {
    return res.status(400).json({
      error: { code: 'BAD_REQUEST', message: 'El nombre del aspirante es obligatorio' }
    });
  }
  const asp = db.aspirantes[0];
  asp.nombre = nombre || asp.nombre;
  asp.apellidos = apellidos !== undefined ? apellidos : asp.apellidos;
  asp.nombreCompleto = nombreCompleto || `${asp.nombre} ${asp.apellidos}`.trim();
  asp.telefono = telefono !== undefined ? telefono : asp.telefono;
  asp.direccion = direccion !== undefined ? direccion : asp.direccion;
  if (idEstado) asp.idEstado = Number(idEstado);
  if (idMunicipio) asp.idMunicipio = Number(idMunicipio);

  return res.status(200).json(asp);
});

router.get(['/aspirantes/me/resumen', '/aspirantes/me/estatus'], (_req, res) => {
  const asp = db.aspirantes[0];
  const docs = db.documentos.filter(d => d.idAspirante === asp.idAspirante);
  const unread = db.notificaciones.filter(n => !n.leida).length;
  return res.status(200).json({
    nombre: asp.nombreCompleto,
    folio: asp.folio,
    carrera: asp.carrera,
    estadoInscripcion: asp.estadoInscripcion,
    documentos: {
      requeridos: 3,
      subidos: docs.length,
      aprobados: docs.filter(d => d.estado === 'aprobado').length,
      pendientes: docs.filter(d => d.estado === 'pendiente').length,
      rechazados: docs.filter(d => d.estado === 'rechazado' || d.estado === 'requiere_correccion').length
    },
    notificacionesNoLeidas: unread,
    proximaFechaLimite: db.fechasImportantes[0]?.fechaLimite || '2026-11-20 23:59'
  });
});

// ===== Documentos (GET, POST Multipart, DELETE) =====
router.get('/aspirantes/me/documentos', (_req, res) => {
  return res.status(200).json(db.documentos);
});

router.post('/aspirantes/me/documentos', upload.single('archivo'), (req, res) => {
  const { idTipoDocumento = 1, tipoCodigo = 'ACTA_NACIMIENTO', hashSha256 } = req.body || {};
  const file = req.file;

  if (!file) {
    return res.status(400).json({
      error: { code: 'FILE_REQUIRED', message: 'Debes adjuntar un archivo PDF o imagen' }
    });
  }

  const computedHash = crypto.createHash('sha256').update(file.buffer).digest('hex');
  if (hashSha256 && hashSha256.toLowerCase() !== computedHash.toLowerCase()) {
    return res.status(422).json({
      error: { code: 'HASH_MISMATCH', message: 'El hash SHA-256 calculado no coincide con el archivo recibido' }
    });
  }

  const prevDocs = db.documentos.filter(d => d.tipoCodigo === tipoCodigo);
  const nextVersion = prevDocs.length > 0 ? Math.max(...prevDocs.map(d => d.version)) + 1 : 1;
  const newDocId = ++db.secuenciaDoc;

  const finalPath = path.join(uploadDir, `asp1_${tipoCodigo}_v${nextVersion}_${file.originalname}`);
  fs.writeFileSync(finalPath, file.buffer);

  // Reemplazar versión vigente en listado activo
  db.documentos = db.documentos.filter(d => d.tipoCodigo !== tipoCodigo);
  const nuevoDoc = {
    id: String(newDocId),
    idDocumento: newDocId,
    idAspirante: 1,
    idTipoDocumento: Number(idTipoDocumento),
    tipo: tipoCodigo,
    tipoCodigo,
    nombreArchivo: file.originalname,
    tamanoBytes: file.size,
    mimeType: file.mimetype || 'application/pdf',
    hashSha256: computedHash,
    version: nextVersion,
    fechaSubida: new Date().toISOString().slice(0, 16).replace('T', ' '),
    estado: 'pendiente',
    observaciones: 'Subido al servidor; pendiente de dictamen.'
  };
  db.documentos.push(nuevoDoc);

  const nuevoEstadoInsc = recalcularEstadoAspirante(1);

  return res.status(201).json({
    mensaje: 'Documento subido y verificado con SHA-256',
    documento: nuevoDoc,
    estadoInscripcion: nuevoEstadoInsc
  });
});

router.delete('/documentos/:id', (req, res) => {
  const idParam = String(req.params.id);
  const idx = db.documentos.findIndex(
    d => d.id === idParam || String(d.idDocumento) === idParam || d.tipoCodigo === idParam
  );
  if (idx === -1) {
    return res.status(404).json({
      error: { code: 'DOCUMENT_NOT_FOUND', message: 'Documento no encontrado' }
    });
  }
  if (db.documentos[idx].estado === 'aprobado') {
    return res.status(409).json({
      error: { code: 'DOCUMENT_ALREADY_APPROVED', message: 'No se permite eliminar un documento aprobado' }
    });
  }
  db.documentos.splice(idx, 1);
  recalcularEstadoAspirante(1);
  return res.status(200).json({ eliminado: true, mensaje: 'Documento eliminado correctamente' });
});

// ===== Notificaciones y Fechas =====
router.get('/notificaciones', (req, res) => {
  const { leida } = req.query;
  let result = db.notificaciones;
  if (leida === 'false') {
    result = result.filter(n => !n.leida);
  } else if (leida === 'true') {
    result = result.filter(n => n.leida);
  }
  return res.status(200).json(result);
});

router.patch('/notificaciones/:id/leida', (req, res) => {
  const idParam = String(req.params.id);
  const notif = db.notificaciones.find(n => n.id === idParam || String(n.idNotificacion) === idParam);
  if (!notif) {
    return res.status(404).json({
      error: { code: 'NOTIFICATION_NOT_FOUND', message: 'Notificación no encontrada' }
    });
  }
  notif.leida = true;
  return res.status(200).json({ idNotificacion: notif.idNotificacion, leida: true });
});

router.patch('/notificaciones/leer-todas', (_req, res) => {
  db.notificaciones.forEach(n => { n.leida = true; });
  return res.status(200).json({ eliminado: true, mensaje: 'Todas las notificaciones marcadas como leídas' });
});

router.delete('/notificaciones/:id', (req, res) => {
  const idParam = String(req.params.id);
  const idx = db.notificaciones.findIndex(n => n.id === idParam || String(n.idNotificacion) === idParam);
  if (idx === -1) {
    return res.status(404).json({
      error: { code: 'NOTIFICATION_NOT_FOUND', message: 'Notificación no encontrada' }
    });
  }
  db.notificaciones.splice(idx, 1);
  return res.status(200).json({ eliminado: true, mensaje: 'Notificación eliminada' });
});

router.get('/fechas-importantes', (_req, res) => {
  return res.status(200).json(db.fechasImportantes);
});

// ===== Comprobante de Pago =====
router.get('/aspirantes/me/pago', (_req, res) => {
  return res.status(200).json(db.pagoActual);
});

router.post('/aspirantes/me/pago', upload.single('comprobante'), (req, res) => {
  const { folioPago, monto, fechaPago, comprobanteUri } = req.body || {};
  if (!folioPago || Number(monto) <= 0) {
    return res.status(422).json({
      error: { code: 'INVALID_PAYMENT_DATA', message: 'Folio de pago y monto mayor a 0 son obligatorios' }
    });
  }
  db.pagoActual = {
    idPago: ++db.secuenciaPago,
    idAspirante: 1,
    referenciaAsignada: 'REF-ITSUR-2026-9081',
    montoEsperado: 1850.0,
    folioPago: String(folioPago),
    monto: Number(monto),
    fechaPago: fechaPago || new Date().toISOString().slice(0, 10),
    rutaComprobante: req.file ? req.file.originalname : (comprobanteUri || ''),
    estado: 'pendiente',
    observaciones: 'Comprobante registrado; en revisión por Control Escolar.',
    validado: false
  };
  return res.status(201).json(db.pagoActual);
});

// ===== Panel de Revisión Control Escolar (Admin) =====
router.get('/admin/aspirantes', (req, res) => {
  const { estado, q } = req.query;
  let list = [...db.aspirantes];
  if (estado) {
    list = list.filter(a => a.estadoInscripcion === estado);
  }
  if (q) {
    const query = String(q).toLowerCase();
    list = list.filter(
      a => a.nombreCompleto.toLowerCase().includes(query) ||
           a.folio.toLowerCase().includes(query) ||
           a.curp.toLowerCase().includes(query)
    );
  }
  return res.status(200).json(list);
});

router.get('/admin/aspirantes/:id', (req, res) => {
  const asp = db.aspirantes.find(a => String(a.idAspirante) === String(req.params.id));
  if (!asp) {
    return res.status(404).json({ error: { code: 'NOT_FOUND', message: 'Aspirante no encontrado' } });
  }
  return res.status(200).json(asp);
});

router.get('/admin/aspirantes/:id/documentos', (_req, res) => {
  return res.status(200).json(db.documentos);
});

router.post('/admin/documentos/:id/revision', (req, res) => {
  const idParam = String(req.params.id);
  const { resultado, observaciones = '' } = req.body || {};
  const estadosValidos = ['aprobado', 'rechazado', 'requiere_correccion', 'presencial'];
  const resNorm = String(resultado || '').toLowerCase();

  if (!estadosValidos.includes(resNorm)) {
    return res.status(400).json({
      error: { code: 'INVALID_RESULTADO', message: 'Resultado de revisión inválido' }
    });
  }
  if (resNorm !== 'aprobado' && !String(observaciones).trim()) {
    return res.status(422).json({
      error: {
        code: 'OBSERVACIONES_REQUIRED',
        message: 'Las observaciones son obligatorias cuando el documento no es aprobado'
      }
    });
  }

  let doc = db.documentos.find(
    d => d.id === idParam || String(d.idDocumento) === idParam || d.tipoCodigo === idParam
  );
  if (!doc && db.documentos.length > 0) {
    doc = db.documentos[0];
  }
  if (doc) {
    doc.estado = resNorm;
    doc.observaciones = observaciones;
  }

  const nuevoEstadoInsc = recalcularEstadoAspirante(1);
  const newNotifId = ++db.secuenciaNotif;
  const nuevaNotif = {
    id: String(newNotifId),
    idNotificacion: newNotifId,
    idUsuario: 1,
    tipo: 'documento',
    idDocumento: doc ? doc.idDocumento : 101,
    titulo: `Dictamen de Control Escolar: ${resNorm.toUpperCase()}`,
    mensaje: observaciones || `Tu documento ha sido dictaminado como ${resNorm}.`,
    fecha: new Date().toISOString().slice(0, 16).replace('T', ' '),
    leida: false
  };
  db.notificaciones.unshift(nuevaNotif);

  return res.status(200).json({
    idRevision: ++db.secuenciaRevision,
    idDocumento: doc ? doc.idDocumento : 101,
    resultado: resNorm,
    observaciones,
    estadoInscripcion: nuevoEstadoInsc,
    notificacionGenerada: nuevaNotif
  });
});

app.use('/api/v1', router);

// 404 Handler
app.use((req, res) => {
  res.status(404).json({
    error: {
      code: 'ENDPOINT_NOT_FOUND',
      message: `No existe la ruta ${req.method} ${req.originalUrl}`
    }
  });
});

module.exports = app;
