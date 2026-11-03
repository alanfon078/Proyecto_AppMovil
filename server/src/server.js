require('dotenv').config();
const app = require('./app');

const PORT = Number(process.env.PORT) || 3000;

app.listen(PORT, '0.0.0.0', () => {
  console.log(`[ITSUR API] Servidor Web API escuchando en http://0.0.0.0:${PORT}/api/v1/`);
  console.log(`[ITSUR API] Accesible desde Emulador Android en http://10.0.2.2:${PORT}/api/v1/`);
});
