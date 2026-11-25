const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const { createHash, randomUUID } = require('node:crypto');
const { z } = require('zod');
const { pool, inTransaction } = require('./db');
const { getAspirante } = require('./aspirantes');
const { HttpError, route } = require('./http');

const accessSeconds = 15 * 60;
const refreshSeconds = 30 * 24 * 60 * 60;
const issuer = 'itsur-inscripciones-api';
const jwtAlgorithm = 'HS256';
const userColumns = 'id_usuario, nombre, apellidos, nombre_usuario, correo, rol, activo';
const passwordInput = z.string().min(1).refine(
  value => Buffer.byteLength(value, 'utf8') <= 72,
  'La contraseña supera el máximo admitido'
);
const newPassword = passwordInput.refine(value => [...value].length >= 8, 'La contraseña debe tener mínimo 8 caracteres');
const refreshInput = z.object({ refresh_token: z.string().min(1).max(4096) });

function checkAuthConfig() {
  const { JWT_SECRET, JWT_REFRESH_SECRET } = process.env;
  if (Buffer.byteLength(JWT_SECRET || '') < 32 || Buffer.byteLength(JWT_REFRESH_SECRET || '') < 32 ||
      JWT_SECRET === JWT_REFRESH_SECRET) {
    throw new Error('Configura JWT_SECRET y JWT_REFRESH_SECRET con secretos distintos de al menos 32 bytes.');
  }
}

function hashToken(token) {
  return createHash('sha256').update(token).digest('hex');
}

function invalidCredentials() {
  return new HttpError(401, 'INVALID_CREDENTIALS', 'Usuario/correo o contraseña inválidos');
}

function invalidRefresh() {
  return new HttpError(401, 'INVALID_REFRESH_TOKEN', 'La sesión no puede renovarse');
}

async function getActiveUser(connection, idUsuario) {
  const [rows] = await connection.execute(
    'SELECT ' + userColumns + ' FROM usuarios WHERE id_usuario = ? AND activo = TRUE FOR UPDATE',
    [idUsuario]
  );
  if (!rows[0]) throw invalidRefresh();
  return rows[0];
}

async function issueSession(connection, user) {
  const subject = String(user.id_usuario);
  const refreshToken = jwt.sign({}, process.env.JWT_REFRESH_SECRET, {
    algorithm: jwtAlgorithm,
    issuer,
    audience: 'itsur-refresh',
    subject,
    jwtid: randomUUID(),
    expiresIn: refreshSeconds
  });
  const [session] = await connection.execute(
    'INSERT INTO refresh_tokens (id_usuario, token_hash, expira_en) VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? SECOND))',
    [user.id_usuario, hashToken(refreshToken), refreshSeconds]
  );
  const accessToken = jwt.sign({ sid: String(session.insertId) }, process.env.JWT_SECRET, {
    algorithm: jwtAlgorithm,
    issuer,
    audience: 'itsur-access',
    subject,
    expiresIn: accessSeconds
  });
  let aspirante = null;
  if (user.rol === 'aspirante') {
    const [rows] = await connection.execute('SELECT id_aspirante FROM aspirantes WHERE id_usuario = ?', [user.id_usuario]);
    if (!rows[0]) throw invalidCredentials();
    aspirante = await getAspirante(connection, rows[0].id_aspirante);
  }
  return {
    access_token: accessToken,
    refresh_token: refreshToken,
    expires_in_seconds: accessSeconds,
    rol: user.rol,
    usuario: {
      id_usuario: user.id_usuario,
      nombre: user.nombre,
      apellidos: user.apellidos,
      nombre_usuario: user.nombre_usuario,
      correo: user.correo,
      rol: user.rol,
      activo: Boolean(user.activo)
    },
    aspirante
  };
}

const requireAuth = route(async (req, res, next) => {
  const challenge = () => {
    res.set('WWW-Authenticate', 'Bearer');
    throw new HttpError(401, 'INVALID_ACCESS_TOKEN', 'Inicia sesión para continuar');
  };
  const token = req.get('Authorization')?.match(/^Bearer ([^\s]+)$/i)?.[1];
  if (!token) return challenge();
  let claims;
  try {
    claims = jwt.verify(token, process.env.JWT_SECRET, {
      algorithms: [jwtAlgorithm],
      issuer,
      audience: 'itsur-access'
    });
  } catch {
    return challenge();
  }
  const [rows] = await pool.execute(
    'SELECT u.id_usuario, u.rol, a.id_aspirante FROM refresh_tokens s' +
    ' JOIN usuarios u ON u.id_usuario = s.id_usuario' +
    ' LEFT JOIN aspirantes a ON a.id_usuario = u.id_usuario' +
    ' WHERE s.id_token = ? AND s.id_usuario = ? AND s.revocado = FALSE' +
    ' AND s.expira_en > NOW() AND u.activo = TRUE',
    [claims.sid, claims.sub]
  );
  const user = rows[0];
  if (!user || (user.rol === 'aspirante' && user.id_aspirante == null)) return challenge();
  req.user = { idUsuario: user.id_usuario, rol: user.rol, idAspirante: user.id_aspirante };
  req.sessionId = claims.sid;
  next();
});

const requireRole = role => (req, _res, next) => {
  if (req.user.rol === role) return next();
  next(new HttpError(403, 'FORBIDDEN', 'No tienes acceso a esta operación'));
};

const login = route(async (req, res) => {
  const { identificador, email, contrasena } = req.body || {};
  const loginId = z.string().trim().min(1).max(150).safeParse(identificador ?? email);
  if (!loginId.success || !passwordInput.safeParse(contrasena).success) throw invalidCredentials();
  const column = loginId.data.includes('@') ? 'correo' : 'nombre_usuario';
  const session = await inTransaction(async connection => {
    const [rows] = await connection.execute(
      'SELECT ' + userColumns + ', password_hash FROM usuarios WHERE ' + column + ' = ? AND activo = TRUE FOR UPDATE',
      [loginId.data.toLowerCase()]
    );
    const user = rows[0];
    if (!user || !await bcrypt.compare(contrasena, user.password_hash)) throw invalidCredentials();
    await connection.execute('UPDATE usuarios SET ultimo_acceso = NOW() WHERE id_usuario = ?', [user.id_usuario]);
    return issueSession(connection, user);
  });
  res.json(session);
});

const registerInput = z.object({
  nombre_completo: z.string().trim().min(1).max(181),
  nombre_usuario: z.string().trim().regex(/^[a-z0-9_.-]{1,50}$/i, 'Nombre de usuario inválido').optional(),
  correo: z.string().trim().toLowerCase().email('El correo electrónico no es válido').max(150),
  contrasena: newPassword,
  curp: z.string().trim().toUpperCase().regex(/^[A-Z]{4}\d{6}[HM][A-Z]{5}[A-Z0-9]\d$/, 'CURP inválida'),
  telefono: z.string().trim().max(15).nullish(),
  direccion: z.string().trim().max(255).nullish(),
  estado_residencia: z.string().trim().min(1, 'Indica el estado de residencia').max(100),
  municipio_residencia: z.string().trim().min(1, 'Indica el municipio de residencia').max(120),
  id_carrera: z.number().int().positive().max(4294967295)
});

const register = route(async (req, res) => {
  const data = registerInput.parse(req.body);
  const [nombre, ...resto] = data.nombre_completo.split(/\s+/);
  const apellidos = resto.join(' ');
  if (nombre.length > 80 || apellidos.length > 100) {
    throw new HttpError(422, 'INVALID_NAME', 'El nombre o los apellidos son demasiado largos');
  }
  const nombreUsuario = data.nombre_usuario?.toLowerCase() || 'aspirante-' + randomUUID();
  const passwordHash = await bcrypt.hash(data.contrasena, 12);
  const session = await inTransaction(async connection => {
    const [carreras] = await connection.execute(
      'SELECT id_carrera FROM carreras WHERE id_carrera = ? AND activa = TRUE FOR SHARE',
      [data.id_carrera]
    );
    if (!carreras[0]) throw new HttpError(422, 'INVALID_CAREER', 'Selecciona una carrera activa');
    const [user] = await connection.execute(
      'INSERT INTO usuarios (nombre, apellidos, nombre_usuario, correo, password_hash) VALUES (?, ?, ?, ?, ?)',
      [nombre, apellidos, nombreUsuario, data.correo, passwordHash]
    );
    const [aspirante] = await connection.execute(
      'INSERT INTO aspirantes (id_usuario, folio, curp, telefono, direccion,' +
      ' estado_residencia, municipio_residencia, id_carrera) VALUES (?, ?, ?, ?, ?, ?, ?, ?)',
      [
        user.insertId, 'TEMP-' + user.insertId, data.curp, data.telefono || null, data.direccion || null,
        data.estado_residencia, data.municipio_residencia, data.id_carrera
      ]
    );
    const year = new Intl.DateTimeFormat('en', { year: 'numeric', timeZone: 'America/Mexico_City' }).format(new Date());
    const folio = 'ITSUR-' + year + '-' + String(aspirante.insertId).padStart(5, '0');
    await connection.execute('UPDATE aspirantes SET folio = ? WHERE id_aspirante = ?', [folio, aspirante.insertId]);
    return issueSession(connection, await getActiveUser(connection, user.insertId));
  });
  res.status(201).json(session);
});

const refresh = route(async (req, res) => {
  const input = refreshInput.safeParse(req.body);
  if (!input.success) throw invalidRefresh();
  const token = input.data.refresh_token;
  let claims;
  try {
    claims = jwt.verify(token, process.env.JWT_REFRESH_SECRET, {
      algorithms: [jwtAlgorithm],
      issuer,
      audience: 'itsur-refresh'
    });
  } catch {
    throw invalidRefresh();
  }
  const session = await inTransaction(async connection => {
    const user = await getActiveUser(connection, claims.sub);
    const [update] = await connection.execute(
      'UPDATE refresh_tokens SET revocado = TRUE WHERE id_usuario = ? AND token_hash = ?' +
      ' AND revocado = FALSE AND expira_en > NOW()',
      [user.id_usuario, hashToken(token)]
    );
    if (!update.affectedRows) throw invalidRefresh();
    return issueSession(connection, user);
  });
  res.json(session);
});

const logout = route(async (req, res) => {
  const { refresh_token: token } = refreshInput.parse(req.body);
  const [result] = await pool.execute(
    'UPDATE refresh_tokens SET revocado = TRUE WHERE id_token = ? AND id_usuario = ? AND token_hash = ?',
    [req.sessionId, req.user.idUsuario, hashToken(token)]
  );
  if (!result.affectedRows) {
    throw new HttpError(422, 'INVALID_REFRESH_TOKEN', 'El token no corresponde a esta sesión');
  }
  res.sendStatus(204);
});

const password = route(async (req, res) => {
  const data = z.object({ contrasena_actual: passwordInput, contrasena_nueva: newPassword }).parse(req.body);
  await inTransaction(async connection => {
    const [rows] = await connection.execute(
      'SELECT password_hash FROM usuarios WHERE id_usuario = ? FOR UPDATE', [req.user.idUsuario]
    );
    if (!rows[0] || !await bcrypt.compare(data.contrasena_actual, rows[0].password_hash)) {
      throw new HttpError(422, 'INVALID_CURRENT_PASSWORD', 'La contraseña actual es incorrecta');
    }
    const hash = await bcrypt.hash(data.contrasena_nueva, 12);
    await connection.execute('UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?', [hash, req.user.idUsuario]);
    await connection.execute(
      'UPDATE refresh_tokens SET revocado = TRUE WHERE id_usuario = ? AND id_token <> ?',
      [req.user.idUsuario, req.sessionId]
    );
  });
  res.sendStatus(204);
});

module.exports = { checkAuthConfig, requireAuth, requireRole, login, register, refresh, logout, password };
