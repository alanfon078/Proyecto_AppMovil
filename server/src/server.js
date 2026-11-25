require('dotenv').config({ path: require('node:path').join(__dirname, '..', '.env') });
const app = require('./app');
const { checkAuthConfig } = require('./auth');
const { pool, initDb } = require('./db');
const { initUploads } = require('./documentos');

const PORT = Number(process.env.PORT) || 3000;

async function startServer() {
  checkAuthConfig();
  await initDb();
  await initUploads();
  app.listen(PORT, '0.0.0.0', () => {
    console.log('[ITSUR API] Servidor escuchando en el puerto ' + PORT);
  }).on('error', async error => {
    console.error('[ITSUR API] No se pudo iniciar:', error.code);
    await pool.end();
    process.exitCode = 1;
  });
}

startServer().catch(async error => {
  console.error('[ITSUR API] No se pudo iniciar:', error.code || error.message);
  await pool.end();
  process.exitCode = 1;
});
