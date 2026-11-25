const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const compression = require('compression');
const { z } = require('zod');
const { pool, inTransaction } = require('./db');
const auth = require('./auth');
const documentos = require('./documentos');
const { seleccionarAspirantes, getAspirante } = require('./aspirantes');
const { HttpError, route, requireFound, idSchema, errorHandler } = require('./http');

const app = express();
app.use(helmet());
app.use(cors());
app.use(compression());
app.use(express.json({ limit: '32kb' }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: 'itsur-inscripciones-api', timestamp: new Date().toISOString() });
});

const router = express.Router();
const { requireAuth, requireRole } = auth;

router.post('/auth/login', auth.login);
router.post('/auth/register', auth.register);
router.post('/auth/refresh', auth.refresh);
router.post('/auth/logout', requireAuth, auth.logout);
router.patch('/auth/password', requireAuth, auth.password);

router.get('/catalogos/carreras', route(async (_req, res) => {
  const [rows] = await pool.execute(
    'SELECT id_carrera, nombre_carrera AS nombre, activa FROM carreras WHERE activa = TRUE ORDER BY nombre_carrera'
  );
  res.json(rows.map(row => ({ ...row, activa: Boolean(row.activa) })));
}));

router.use('/aspirantes/me', requireAuth, requireRole('aspirante'));
router.use('/admin', requireAuth, requireRole('escolar'));

router.get('/aspirantes/me', route(async (req, res) => {
  res.json(await getAspirante(pool, req.user.idAspirante));
}));

const perfilInput = z.object({
  telefono: z.string().trim().max(15).nullable().optional(),
  direccion: z.string().trim().max(255).nullable().optional(),
  estado_residencia: z.string().trim().min(1, 'Indica el estado de residencia').max(100).optional(),
  municipio_residencia: z.string().trim().min(1, 'Indica el municipio de residencia').max(120).optional()
}).refine(data => Object.keys(data).length > 0, 'No hay datos de contacto o residencia para actualizar');

router.put('/aspirantes/me', route(async (req, res) => {
  const data = perfilInput.parse(req.body);
  const fields = Object.keys(data).map(field => field + ' = ?');
  const id = req.user.idAspirante;
  const result = await inTransaction(async connection => {
    await connection.execute(
      'UPDATE aspirantes SET ' + fields.join(', ') + ' WHERE id_aspirante = ?',
      [...Object.values(data), id]
    );
    return getAspirante(connection, id);
  });
  res.json(result);
}));

router.get(['/aspirantes/me/resumen', '/aspirantes/me/estatus'], route(async (req, res) => {
  const { idAspirante, idUsuario } = req.user;
  const aspirante = await getAspirante(pool, idAspirante);
  const [conteo, [unread], [fechas]] = await Promise.all([
    documentos.getConteoDocumentos(pool, idAspirante),
    pool.execute('SELECT COUNT(*) AS total FROM notificaciones WHERE id_usuario = ? AND leida = FALSE', [idUsuario]),
    pool.execute(
      'SELECT fecha_limite FROM fechas_importantes WHERE activa = TRUE AND fecha_limite >= NOW()' +
      ' AND (id_carrera IS NULL OR id_carrera = ?) ORDER BY fecha_limite LIMIT 1',
      [aspirante.id_carrera]
    )
  ]);
  res.json({
    aspirante,
    documentos: conteo,
    notificaciones_no_leidas: unread[0].total,
    proxima_fecha_limite: fechas[0]?.fecha_limite ?? null
  });
}));

router.get('/aspirantes/me/documentos', documentos.getMisDocumentos);
router.post('/aspirantes/me/documentos', documentos.recibirArchivo, documentos.subirDocumento);
router.delete('/documentos/:id', requireAuth, requireRole('aspirante'), documentos.eliminarDocumento);

const notificacionQuery = z.object({
  leida: z.enum(['true', 'false']).transform(value => value === 'true').optional(),
  afterId: z.union([idSchema, z.literal('0')]).optional()
});
router.use('/notificaciones', requireAuth);

router.get('/notificaciones', route(async (req, res) => {
  const { leida, afterId } = notificacionQuery.parse(req.query);
  let sql = 'SELECT id_notificacion, id_usuario, tipo, id_documento, id_fecha, titulo, mensaje, fecha_envio, leida' +
    ' FROM notificaciones WHERE id_usuario = ?';
  const params = [req.user.idUsuario];
  if (leida !== undefined) {
    sql += ' AND leida = ?';
    params.push(leida);
  }
  if (afterId !== undefined) {
    sql += ' AND id_notificacion > ? ORDER BY id_notificacion ASC';
    params.push(afterId);
  } else {
    sql += ' ORDER BY id_notificacion DESC';
  }
  const [rows] = await pool.execute(sql, params);
  res.json(rows.map(row => ({ ...row, leida: Boolean(row.leida) })));
}));

router.patch('/notificaciones/leer-todas', route(async (req, res) => {
  await pool.execute(
    'UPDATE notificaciones SET leida = TRUE, fecha_lectura = NOW() WHERE id_usuario = ? AND leida = FALSE',
    [req.user.idUsuario]
  );
  res.sendStatus(204);
}));

router.patch('/notificaciones/:id/leida', route(async (req, res) => {
  const [result] = await pool.execute(
    'UPDATE notificaciones SET leida = TRUE, fecha_lectura = COALESCE(fecha_lectura, NOW())' +
    ' WHERE id_usuario = ? AND id_notificacion = ?',
    [req.user.idUsuario, idSchema.parse(req.params.id)]
  );
  requireFound(result.affectedRows, 'NOTIFICATION_NOT_FOUND', 'Notificación no encontrada');
  res.sendStatus(204);
}));

router.delete('/notificaciones/:id', route(async (req, res) => {
  const [result] = await pool.execute(
    'DELETE FROM notificaciones WHERE id_usuario = ? AND id_notificacion = ?',
    [req.user.idUsuario, idSchema.parse(req.params.id)]
  );
  requireFound(result.affectedRows, 'NOTIFICATION_NOT_FOUND', 'Notificación no encontrada');
  res.sendStatus(204);
}));

const seleccionarFechas = 'SELECT id_fecha, titulo, descripcion, tipo, fecha_limite, id_carrera, activa,' +
  ' DATEDIFF(fecha_limite, CURRENT_DATE) AS dias_restantes FROM fechas_importantes';

router.get('/fechas-importantes', requireAuth, route(async (req, res) => {
  const esAspirante = req.user.rol === 'aspirante';
  const condiciones = esAspirante
    ? ' AND fecha_limite >= NOW() AND (id_carrera IS NULL OR id_carrera = ' +
      '(SELECT id_carrera FROM aspirantes WHERE id_aspirante = ?))'
    : '';
  const [rows] = await pool.execute(
    seleccionarFechas + ' WHERE activa = TRUE' + condiciones + ' ORDER BY fecha_limite, id_fecha',
    esAspirante ? [req.user.idAspirante] : []
  );
  res.json(rows.map(row => ({ ...row, activa: Boolean(row.activa) })));
}));

function normalizarFechaLimite(raw) {
  const match = raw.match(/^([1-9]\d{3}-\d{2}-\d{2})(?:[T ](\d{2}:\d{2})(?::(\d{2}))?)?$/);
  if (!match) throw new HttpError(422, 'INVALID_DATE', 'Indica una fecha válida con formato AAAA-MM-DD');
  const hora = match[2] || '23:59';
  const segundos = match[3] || (match[2] ? '00' : '59');
  const iso = match[1] + 'T' + hora + ':' + segundos + '.000Z';
  const fecha = new Date(iso);
  if (Number.isNaN(fecha.valueOf()) || fecha.toISOString() !== iso) {
    throw new HttpError(422, 'INVALID_DATE', 'Indica una fecha válida con formato AAAA-MM-DD');
  }
  return iso.slice(0, 19).replace('T', ' ');
}

router.post('/admin/fechas-importantes', route(async (req, res) => {
  const data = z.object({
    titulo: z.string().trim().min(1).max(150),
    descripcion: z.string().trim().max(2000).nullish(),
    tipo: z.enum(['limite_entrega', 'correcciones', 'revision', 'publicacion', 'otro']).default('limite_entrega'),
    fecha_limite: z.string().trim()
  }).parse(req.body);
  const [result] = await pool.execute(
    'INSERT INTO fechas_importantes (titulo, descripcion, tipo, fecha_limite, creada_por) VALUES (?, ?, ?, ?, ?)',
    [data.titulo, data.descripcion || null, data.tipo, normalizarFechaLimite(data.fecha_limite), req.user.idUsuario]
  );
  const [[fecha]] = await pool.execute(seleccionarFechas + ' WHERE id_fecha = ?', [result.insertId]);
  res.status(201).json({ ...fecha, activa: Boolean(fecha.activa) });
}));

const filtroAspirantes = z.object({
  estado: z.enum(['en_captura', 'en_revision', 'con_observaciones', 'documentacion_aprobada', 'rechazado']).optional(),
  q: z.string().trim().max(150).optional()
});
router.get('/admin/aspirantes', route(async (req, res) => {
  const { estado, q } = filtroAspirantes.parse(req.query);
  const filtros = [];
  const params = [];
  if (estado) {
    filtros.push('a.estado_inscripcion = ?');
    params.push(estado);
  }
  if (q) {
    filtros.push("(LOCATE(?, CONCAT_WS(' ', u.nombre, u.apellidos)) > 0 OR LOCATE(?, a.folio) > 0 OR LOCATE(?, a.curp) > 0)");
    params.push(q, q, q);
  }
  const where = filtros.length ? ' WHERE ' + filtros.join(' AND ') : '';
  const [rows] = await pool.execute(seleccionarAspirantes + where + ' ORDER BY a.id_aspirante', params);
  res.json(rows);
}));

router.get('/admin/aspirantes/:id', route(async (req, res) => {
  res.json(await getAspirante(pool, idSchema.parse(req.params.id)));
}));
router.get('/admin/aspirantes/:id/documentos', documentos.getDocumentosAspirante);
router.post('/admin/documentos/:id/revision', documentos.revisarDocumento);
router.post('/admin/aspirantes/:id/aprobar', documentos.aprobarTramite);

app.use('/api/v1', router);
app.use((_req, res, next) => next(new HttpError(404, 'ENDPOINT_NOT_FOUND', 'La ruta solicitada no existe')));
app.use(errorHandler);

module.exports = app;
