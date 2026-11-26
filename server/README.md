# API de inscripciones ITSUR

## Configuración

La API requiere Node.js y MySQL 8. Copia el archivo `.env.example` a `.env`, configura el usuario y la contraseña de MySQL, y genera valores distintos para `JWT_SECRET` y `JWT_REFRESH_SECRET`. Esta instrucción genera un secreto. Ejecútala dos veces.

```sh
node -e "console.log(require('node:crypto').randomBytes(32).toString('hex'))"
```

Al iniciar, Node.js crea `DB_NAME` si hace falta y restaura `database/bd_inscripciones_itsur.bak` si el esquema está vacío. El archivo SQL contiene el esquema y los catálogos institucionales. La cuenta de MySQL del primer arranque debe poder crear el esquema y sus objetos, insertar los catálogos y borrar el marcador de restauración al finalizar. Si el esquema ya tiene tablas, la API lo usa directamente con los permisos habituales. Una restauración interrumpida bloquea el arranque hasta revisar esa instalación.

La cuenta que restaura el esquema queda como definidora del trigger de notificaciones. Conserva esa cuenta y los permisos que el trigger usa al insertar notificaciones. MySQL puede exigir permisos adicionales para crearlo si el registro binario está habilitado. [Manual de `CREATE TRIGGER`](https://dev.mysql.com/doc/refman/8.0/en/create-trigger.html)

Para actualizar una base existente, genera un respaldo de sus datos, detén la API y aplica las migraciones pendientes en orden.

1. `database/migrations/001_f07.sql` para instalaciones previas a F07. Conserva las versiones de documentos y copia la residencia de los catálogos anteriores.
2. `database/migrations/002_f09.sql` para instalaciones previas a F09. Agrega las preguntas y sus notificaciones.
3. `database/migrations/003_orden_notificaciones.sql` para instalaciones F09 existentes y para las bases que acaban de pasar por las migraciones anteriores. Aplícala con una cuenta autorizada para crear el trigger y bloquear la fila del destinatario en `usuarios`.

El respaldo previo a la migración conserva los registros de pagos y firmas de reglamento que `001_f07.sql` retira de la base activa.

Configura en `DB_USER` la cuenta que usa la API para consultar y modificar los registros. Instala las dependencias con `npm ci` desde `server`, inicia con `npm start` y comprueba `GET /health`. Configura `UPLOAD_DIR` en una ubicación privada y persistente. El directorio forma parte del respaldo junto con la base de datos.

## Conexión con Android

Por defecto, la app de depuración usa `http://10.0.2.2:3000/api/v1/` para conectar desde el emulador. Define `ITSUR_BASE_URL` como propiedad de Gradle o variable de entorno al compilar para usar otra dirección. La URL debe incluir la ruta `/api/v1/`.

## Acceso administrativo

La API inicia con el catálogo institucional. Un administrador de la base debe provisionar la cuenta de Control Escolar en `usuarios` con `rol = 'escolar'`, un nombre de usuario y correo únicos, y un hash bcrypt de la contraseña temporal. Usa bcrypt con un coste de 12 y una contraseña de al menos ocho caracteres y como máximo 72 bytes. Las cuentas que sembraba el servidor anterior quedan desactivadas al migrar. Si corresponde recuperar alguna, cambia su contraseña y actívala mediante el procedimiento administrativo.
