const { ZodError, z } = require('zod');

class HttpError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

const route = handler => (req, res, next) => {
  Promise.resolve(handler(req, res, next)).catch(next);
};

function requireFound(value, code, message) {
  if (!value) throw new HttpError(404, code, message);
  return value;
}

const idSchema = z.string().refine(
  value => /^[1-9]\d{0,19}$/.test(value) && BigInt(value) <= 18446744073709551615n,
  'Identificador inválido'
);

function errorHandler(error, _req, res, next) {
  if (res.headersSent) return next(error);
  if (error instanceof HttpError) {
    return res.status(error.status).json({ error: { code: error.code, message: error.message } });
  }
  if (error instanceof ZodError) {
    return res.status(422).json({
      error: { code: 'VALIDATION_ERROR', message: error.issues[0].message }
    });
  }
  if (error.code === 'ER_DUP_ENTRY') {
    return res.status(409).json({
      error: { code: 'DUPLICATE_ASPIRANTE', message: 'La CURP, el correo o el usuario ya están registrados' }
    });
  }
  if (error.type === 'entity.parse.failed') {
    return res.status(400).json({ error: { code: 'INVALID_BODY', message: 'El JSON enviado no es válido' } });
  }
  if (error.type === 'entity.too.large') {
    return res.status(413).json({ error: { code: 'BODY_TOO_LARGE', message: 'El cuerpo de la solicitud es demasiado grande' } });
  }
  console.error('[API]', error.code || error.name);
  return res.status(500).json({ error: { code: 'SERVER_ERROR', message: 'Error interno del servidor' } });
}

module.exports = { HttpError, route, requireFound, idSchema, errorHandler };
