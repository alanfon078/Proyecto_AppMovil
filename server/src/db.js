const mysql = require('mysql2/promise');

const pool = mysql.createPool({
  host: process.env.DB_HOST || '127.0.0.1',
  port: Number(process.env.DB_PORT) || 3306,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME || 'bd_inscripciones_itsur',
  waitForConnections: true,
  connectionLimit: 10,
  charset: 'utf8mb4',
  supportBigNumbers: true,
  decimalNumbers: true,
  dateStrings: true,
  timezone: 'Z'
});

async function initDb() {
  await pool.query('SELECT estado_residencia, municipio_residencia FROM aspirantes LIMIT 0');
  await pool.query('SELECT vigente FROM documentos LIMIT 0');
  await pool.query('SELECT token_hash FROM refresh_tokens LIMIT 0');
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
