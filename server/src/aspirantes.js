const { requireFound } = require('./http');

const seleccionarAspirantes =
  "SELECT a.id_aspirante, a.id_usuario, a.folio, CONCAT_WS(' ', u.nombre, NULLIF(u.apellidos, '')) AS nombre_completo," +
  ' u.correo, a.curp, a.telefono, a.direccion, a.estado_residencia, a.municipio_residencia, a.id_carrera,' +
  ' c.nombre_carrera AS carrera, a.promedio_bachillerato, a.estado_inscripcion, a.fecha_registro' +
  ' FROM aspirantes a JOIN usuarios u ON u.id_usuario = a.id_usuario' +
  ' JOIN carreras c ON c.id_carrera = a.id_carrera';

async function getAspirante(connection, idAspirante) {
  const [rows] = await connection.execute(
    seleccionarAspirantes + ' WHERE a.id_aspirante = ?',
    [idAspirante]
  );
  return requireFound(rows[0], 'ASPIRANTE_NOT_FOUND', 'Aspirante no encontrado');
}

async function bloquearAspirante(connection, idAspirante) {
  const [rows] = await connection.execute(
    'SELECT id_usuario, estado_inscripcion FROM aspirantes WHERE id_aspirante = ? FOR UPDATE',
    [idAspirante]
  );
  return requireFound(rows[0], 'ASPIRANTE_NOT_FOUND', 'Aspirante no encontrado');
}

module.exports = { seleccionarAspirantes, getAspirante, bloquearAspirante };
