# D) Diagrama UML de Despliegue (Arquitectura Física y de Red)

**Proyecto:** Sistema de Inscripciones ITSUR (Nuevo Ingreso)  
**Entorno de Ejecución:** Cliente Móvil Android (API 29+) ⇄ Servidor Web API REST Node.js ⇄ MySQL 8 + Sistema de Archivos `/uploads`

---

## 1. Diagrama UML de Despliegue Completo

```mermaid
flowchart TB
    subgraph NODE_CLIENT["«device» Nodo Dispositivo Móvil Android (Android 10+ / API 29..37)"]
        subgraph ART_APK["«artifact» app-debug.apk (Kotlin 2.2 + Jetpack Compose)"]
            UI_LAYER["Jetpack Compose UI + Material 3\nViewModels (StateFlow)"]
            REPO_CLIENT["InscripcionesRepositoryImpl\n+ FileHashHelper (SHA-256)"]
            RETROFIT_NET["Retrofit 2.11 + OkHttp 4.12\nAuthInterceptor (Bearer JWT)\nGsonConverterFactory"]
        end

        subgraph LOCAL_STORAGE["Almacenamiento Privado del Dispositivo (/data/data/com.example.proyecto_appmovil)"]
            SQLITE_DB[("«database»\nSQLite 3 · Room v1\nbd_inscripciones_itsur.db")]
            PREFS_DS[("«datastore»\nitsur_session_prefs\nTokens JWT + Rol")]
            INTERNAL_FILES[("«filesystem»\n/files/documentos/\nCopias locales PDF / JPG")]
        end

        UI_LAYER --> REPO_CLIENT
        REPO_CLIENT --> RETROFIT_NET
        REPO_CLIENT --> SQLITE_DB
        REPO_CLIENT --> PREFS_DS
        REPO_CLIENT --> INTERNAL_FILES
    end

    subgraph NET_LINK["«communication path» Red TCP/IP · HTTP/HTTPS REST (Puerto 3000)"]
        PROTOCOL["Protocolo REST / HTTP 1.1 & HTTPS\n• Emulador Android: http://10.0.2.2:3000/api/v1/\n• Payloads: application/json (UTF-8)\n• Subida de archivos: multipart/form-data\n• Cabecera: Authorization: Bearer <JWT>"]
    end

    subgraph NODE_SERVER["«executionEnvironment» Nodo Servidor de Aplicaciones (Node.js v20+ / Express 4)"]
        subgraph ART_SERVER["«artifact» /server (itsur-inscripciones-api)"]
            SEC_MW["Middlewares de Seguridad y Transporte\n• Helmet · CORS · Compression · Rate-Limit\n• JWT Auth & Role Guards ('aspirante' | 'escolar')"]
            MULTER_SVC["Multer + Verificador Criptográfico\n• Validación MIME y tamaño (<= 5 MB)\n• Verificación de integridad SHA-256"]
            CONTROLLERS["Controladores y Servicios REST\n• /auth · /catalogos · /aspirantes\n• /documentos · /notificaciones\n• /fechas-importantes · /admin"]
            MYSQL_POOL["Conector mysql2/promise\nPool de Conexiones Transaccionales"]
        end

        subgraph SERVER_FS["«filesystem» Sistema de Archivos del Servidor"]
            UPLOADS_DIR[("Directorio /server/uploads/\nResguardo físico de PDFs versionados,\nfotos de credencial y comprobantes")]
        end

        SEC_MW --> MULTER_SVC
        MULTER_SVC --> CONTROLLERS
        MULTER_SVC --> UPLOADS_DIR
        CONTROLLERS --> MYSQL_POOL
    end

    subgraph NODE_DB["«databaseServer» Nodo Servidor de Base de Datos (MySQL 8.0 · Puerto 3306)"]
        MYSQL_ENGINE[("«RDBMS»\nMySQL 8.0 · Motor InnoDB\nbd_inscripciones_itsur\n13 Tablas Relacionales + Vistas\nTransacciones ACID")]
    end

    RETROFIT_NET == "JSON / multipart/form-data" ==> PROTOCOL
    PROTOCOL == "Códigos HTTP 200, 201, 400, 401, 404, 409, 422" ==> SEC_MW
    MYSQL_POOL == "TCP 3306 · SQL Prepared Statements" ==> MYSQL_ENGINE
```

---

## 2. Especificación Técnica de Nodos y Protocolos de Comunicación

| Nodo / Artefacto | Tecnología / Especificación | Responsabilidad en la Arquitectura |
| :--- | :--- | :--- |
| **Nodo Dispositivo Android** | Android SDK 29–37, Kotlin 2.2, Jetpack Compose, Coroutines & `StateFlow` | Ejecución nativa de las 10 pantallas del sistema, captura multimedia con SAF/Cámara, cálculo local de hash `SHA-256` y operación *offline-first*. |
| **Base de Datos Local (Room)** | SQLite 3 (`bd_inscripciones_itsur.db`), 10 entidades Room y 6 DAOs reactivos | Persistencia local de sesión (`UsuarioEntity`, `AspiranteEntity`), documentos (`DocumentoEntity`), pagos (`ComprobantePagoEntity`), notificaciones (`NotificacionEntity`), fechas y catálogos (`CarreraEntity`, `EstadoEntity`, `MunicipioEntity`). |
| **Canal de Comunicación** | HTTP/HTTPS REST (`http://10.0.2.2:3000/api/v1/` en emulador) | Transporte de payloads `application/json` y archivos binarios `multipart/form-data` autenticados con `Authorization: Bearer <accessToken>`. |
| **Nodo Servidor Web API** | Node.js + Express (`server/src/app.js`), Multer, Helmet, CORS, Crypto | Exposición del contrato REST `/api/v1/*`, validación de integridad SHA-256, cálculo automático de estado de inscripción y emisión de notificaciones. |
| **Sistema de Archivos (`/uploads`)** | Almacenamiento en disco (`server/uploads/`) | Resguardo seguro de los archivos PDF de documentos obligatorios, fotografías de credencial y comprobantes bancarios fuera de la raíz pública. |
| **Nodo Base de Datos Relacional** | MySQL 8.0 (`InnoDB`, `utf8mb4`), esquema `server/database/schema.sql` | Almacenamiento relacional centralizado con integridad referencial (`FOREIGN KEY`), restricciones de dominio (`ENUM`, `CHECK`, `UNIQUE`) y control de versiones de documentos. |
