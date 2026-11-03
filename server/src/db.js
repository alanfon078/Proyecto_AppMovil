const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt');

const pool = mysql.createPool({
  host: process.env.DB_HOST || '127.0.0.1',
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || 'root',
  database: process.env.DB_NAME || 'bd_inscripciones_itsur',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
  charset: 'utf8mb4'
});

async function initDb() {
  try {
    const conn = await pool.getConnection();
    console.log('[MySQL] Conexión establecida con éxito a la base de datos:', process.env.DB_NAME || 'bd_inscripciones_itsur');

    // 1. Catálogo Carreras
    const [carreras] = await conn.query('SELECT COUNT(*) as count FROM carreras');
    if (carreras[0].count === 0) {
      console.log('[MySQL] Sembrando catálogo de carreras...');
      const listaCarreras = [
        [1, 'Ing. Sistemas Computacionales', true],
        [2, 'Ing. Sistemas Automotrices', true],
        [3, 'Ing. Ambiental', true],
        [4, 'Lic. Gestión Empresarial', true],
        [5, 'Ing. Industrial', true],
        [6, 'Ing. Electrónica', true],
        [7, 'Ing. Semiconductores', true],
        [8, 'Lic. Gastronomía y Turismo', true]
      ];
      await conn.query('INSERT INTO carreras (id_carrera, nombre_carrera, activa) VALUES ?', [listaCarreras]);
    }

    // 2. Catálogo Estados
    const [estados] = await conn.query('SELECT COUNT(*) as count FROM estados');
    if (estados[0].count === 0) {
      console.log('[MySQL] Sembrando catálogo de estados...');
      const listaEstados = [
        [11, 'Guanajuato'],
        [16, 'Michoacán'],
        [14, 'Jalisco'],
        [22, 'Querétaro']
      ];
      await conn.query('INSERT INTO estados (id_estado, nombre) VALUES ?', [listaEstados]);
    }

    // 3. Catálogo Municipios
    const [municipios] = await conn.query('SELECT COUNT(*) as count FROM municipios');
    if (municipios[0].count === 0) {
      console.log('[MySQL] Sembrando catálogo de municipios...');
      const listaMunicipios = [
        [1, 11, 'Uriangato'],
        [2, 11, 'Moroleón'],
        [3, 11, 'Yuriria'],
        [4, 11, 'Salvatierra'],
        [5, 11, 'Valle de Santiago'],
        [6, 11, 'Acámbaro'],
        [7, 11, 'Celaya'],
        [8, 11, 'León'],
        [101, 16, 'Morelia'],
        [102, 16, 'Puruándiro'],
        [103, 16, 'Santa Ana Maya']
      ];
      await conn.query('INSERT INTO municipios (id_municipio, id_estado, nombre) VALUES ?', [listaMunicipios]);
    }

    // 4. Catálogo Tipos de Documento
    const [tiposDoc] = await conn.query('SELECT COUNT(*) as count FROM tipos_documento');
    if (tiposDoc[0].count === 0) {
      console.log('[MySQL] Sembrando catálogo de tipos de documento...');
      const listaTiposDoc = [
        [1, 'ACTA_NACIMIENTO', 'Acta de Nacimiento', 'Formato PDF legible y reciente', true, 'application/pdf', 5242880, 1],
        [2, 'CERTIFICADO_BACHILLERATO', 'Certificado de Bachillerato', 'O constancia con promedio', true, 'application/pdf', 5242880, 2],
        [3, 'CURP', 'CURP Certificada', 'Formato actualizado emitido por RENAPO', true, 'application/pdf', 5242880, 3],
        [4, 'COMPROBANTE_DOMICILIO', 'Comprobante de Domicilio', 'Agua, luz o teléfono (máx. 3 meses)', true, 'application/pdf', 5242880, 4]
      ];
      await conn.query('INSERT INTO tipos_documento (id_tipo_documento, codigo, nombre_documento, descripcion, obligatorio, formatos_permitidos, max_bytes, orden) VALUES ?', [listaTiposDoc]);
    }

    // 5. Usuario Administrador Escolar si no existe
    const [adminCheck] = await conn.query('SELECT id_usuario FROM usuarios WHERE rol = "escolar" LIMIT 1');
    if (adminCheck.length === 0) {
      console.log('[MySQL] Creando usuario administrador de Control Escolar...');
      const hashAdmin = await bcrypt.hash('admin123', 10);
      await conn.query(
        `INSERT INTO usuarios (id_usuario, nombre, apellidos, nombre_usuario, correo, password_hash, rol, activo)
         VALUES (99, 'Coordinación', 'Control Escolar', 'escolar.admin', 'escolar.admin@itsur.edu.mx', ?, 'escolar', true)`,
        [hashAdmin]
      );
    }

    // 6. Aspirante inicial de prueba si no existe
    const [aspCheck] = await conn.query('SELECT id_aspirante FROM aspirantes LIMIT 1');
    if (aspCheck.length === 0) {
      console.log('[MySQL] Creando usuario y aspirante inicial de prueba...');
      const hashAsp = await bcrypt.hash('itsur2026', 10);
      const [uRes] = await conn.query(
        `INSERT INTO usuarios (nombre, apellidos, nombre_usuario, correo, password_hash, rol, activo)
         VALUES ('Alan Yael', 'Fonseca Ruiz', 'alan.fonseca', 'alan.fonseca@alumnos.itsur.edu.mx', ?, 'aspirante', true)`,
        [hashAsp]
      );
      const idUsuario = uRes.insertId;
      await conn.query(
        `INSERT INTO aspirantes (id_usuario, folio, curp, telefono, direccion, id_estado, id_municipio, id_carrera, promedio_bachillerato, estado_inscripcion)
         VALUES (?, 'ITSUR-2026-00001', 'FORA040101HGTRLA01', '4451234567', 'Av. Educación Superior 2000, Benito Juárez', 11, 1, 1, 9.4, 'en_revision')`,
        [idUsuario]
      );
    }

    conn.release();
    console.log('[MySQL] Inicialización de base de datos completada.');
  } catch (error) {
    console.error('[MySQL ERROR] Fallo al inicializar base de datos:', error.message);
  }
}

module.exports = {
  pool,
  initDb
};
