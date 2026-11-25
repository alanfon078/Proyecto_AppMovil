# API de inscripciones ITSUR

## Configuración

La API requiere Node.js y MySQL 8. Copia el archivo `.env.example` a `.env`, configura el usuario y la contraseña de MySQL, y genera valores distintos para `JWT_SECRET` y `JWT_REFRESH_SECRET`. Esta instrucción genera un secreto. Ejecútala dos veces.

```sh
node -e "console.log(require('node:crypto').randomBytes(32).toString('hex'))"
```

Importa `database/schema.sql` con un usuario autorizado para crear la base de datos si es una instalación nueva. Para una base previa a F07, respalda la base y ejecuta una sola vez `database/migrations/001_f07.sql` sobre esa base antes de arrancar la API. La migración conserva las versiones históricas de los documentos y copia la residencia de los catálogos anteriores. El respaldo también conserva los registros de pagos y firmas de reglamento que la migración retira de la base activa.

Configura en `DB_USER` la cuenta que usa la API para consultar y modificar los registros. Instala las dependencias con `npm ci` desde `server`, inicia con `npm start` y comprueba `GET /health`. Configura `UPLOAD_DIR` en una ubicación privada y persistente. El directorio forma parte del respaldo junto con la base de datos.

## Conexión con Android

Por defecto, la app de depuración usa `http://10.0.2.2:3000/api/v1/` para conectar desde el emulador. Define `ITSUR_BASE_URL` como propiedad de Gradle o variable de entorno al compilar para usar otra dirección. La URL debe incluir la ruta `/api/v1/`.

## Acceso administrativo

La API inicia con el catálogo institucional. Un administrador de la base debe provisionar la cuenta de Control Escolar en `usuarios` con `rol = 'escolar'`, un nombre de usuario y correo únicos, y un hash bcrypt de la contraseña temporal. Usa bcrypt con un coste de 12 y una contraseña de al menos ocho caracteres y como máximo 72 bytes. Las cuentas que sembraba el servidor anterior quedan desactivadas al migrar. Si corresponde recuperar alguna, cambia su contraseña y actívala mediante el procedimiento administrativo.
