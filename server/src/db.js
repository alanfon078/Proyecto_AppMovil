const { createHash } = require('node:crypto');
const { readFile } = require('node:fs/promises');
const { join } = require('node:path');
const mysql = require('mysql2/promise');

const database = process.env.DB_NAME || 'bd_inscripciones_itsur';
const connectionOptions = {
  host: process.env.DB_HOST || '127.0.0.1',
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  charset: 'utf8mb4'
};

const pool = mysql.createPool({
  ...connectionOptions,
  database,
  waitForConnections: true,
  connectionLimit: 10,
  supportBigNumbers: true,
  decimalNumbers: true,
  dateStrings: true,
  timezone: 'Z'
});

async function restoreInitialDatabase() {
  const connection = await mysql.createConnection({ ...connectionOptions, multipleStatements: true });
  const lockName = createHash('sha256').update(`itsur:restauracion:${database.toLowerCase()}`).digest('hex');
  const pendingTable = '_itsur_restauracion_pendiente';

  try {
    const [[{ acquired }]] = await connection.execute('SELECT GET_LOCK(?, 30) AS acquired', [lockName]);
    if (acquired !== 1) {
      throw new Error('No se obtuvo el bloqueo para verificar el respaldo inicial de MySQL.');
    }

    try {
      await connection.query('USE ??', [database]);
    } catch (error) {
      if (error.code !== 'ER_BAD_DB_ERROR') throw error;
      await connection.query('CREATE DATABASE IF NOT EXISTS ?? CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci', [database]);
      await connection.query('USE ??', [database]);
    }

    const [tables] = await connection.query({ sql: 'SHOW FULL TABLES', rowsAsArray: true });
    if (tables.some(([name]) => name === pendingTable)) {
      throw new Error('La restauración inicial de MySQL quedó incompleta. Revisa la instalación antes de reiniciar la API.');
    }
    if (tables.length > 0) return;

    const backup = await readFile(join(__dirname, '..', 'database', 'bd_inscripciones_itsur.bak'), 'utf8');
    // El marcador permite detectar una restauración interrumpida porque MySQL confirma cada DDL.
    await connection.query('CREATE TABLE ?? (id TINYINT PRIMARY KEY) ENGINE=InnoDB', [pendingTable]);
    await connection.query(backup);
    await connection.query('DROP TABLE ??', [pendingTable]);
    console.log('[MySQL] Respaldo inicial restaurado.');
  } finally {
    await connection.end();
  }
}

async function initDb() {
  await restoreInitialDatabase();
  await pool.query('SELECT estado_residencia, municipio_residencia FROM aspirantes LIMIT 0');
  await pool.query('SELECT vigente FROM documentos LIMIT 0');
  await pool.query('SELECT token_hash FROM refresh_tokens LIMIT 0');
  const [tiposDocumento] = await pool.query('SELECT id_tipo_documento FROM tipos_documento LIMIT 1');
  if (tiposDocumento.length === 0) {
    throw new Error('La base de datos debe incluir el catálogo de tipos de documento antes de iniciar la API.');
  }
  try {
    await pool.query('SELECT id_pregunta FROM preguntas_dudas LIMIT 0');
    await pool.query('SELECT id_pregunta FROM notificaciones LIMIT 0');
  } catch (error) {
    if (['ER_NO_SUCH_TABLE', 'ER_BAD_FIELD_ERROR'].includes(error.code)) {
      throw new Error('Aplica database/migrations/002_f09.sql antes de iniciar la API.');
    }
    throw error;
  }
}

async function inTransaction(work) {
  const connection = await pool.getConnection();
  try {
    await connection.query('SET TRANSACTION ISOLATION LEVEL READ COMMITTED');
    await connection.beginTransaction();
    const result = await work(connection);
    await connection.commit();
    return result;
  } catch (error) {
    try {
      await connection.rollback();
    } catch (rollbackError) {
      console.error('[MySQL] No se pudo revertir la transacción:', rollbackError.code);
    }
    throw error;
  } finally {
    connection.release();
  }
}

module.exports = { pool, initDb, inTransaction };
