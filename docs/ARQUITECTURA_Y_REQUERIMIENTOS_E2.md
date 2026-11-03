# Documentación Formal de Requerimientos, Transacciones CRUD y Arquitectura (Segundo Entregable)

**Proyecto:** Sistema de Inscripciones ITSUR (Nuevo Ingreso)  
**Rama:** `feature/transacciones-crud-documentacion-uml`  
**Fecha:** 02/11/2026

---

## 1. Índice de Entregables de Arquitectura (Carpeta `docs/`)

1. **[01_diagrama_base_datos_relacional.md](./01_diagrama_base_datos_relacional.md)**: Diagrama Entidad-Relación y Relacional completo con las 13 entidades del sistema (`usuarios`, `aspirantes`, `documentos`, `tipos_documento`, `revisiones`, `notificaciones`, `fechas_importantes`, `pagos`, `firmas_reglamento`, `carreras`, `estados`, `municipios`, `refresh_tokens`), especificando PK, FK, tipos MySQL y restricciones (`UNIQUE`, `NOT NULL`, `CHECK`, `ENUM`).
2. **[02_diagrama_uml_paquetes.md](./02_diagrama_uml_paquetes.md)**: Diagrama UML de Paquetes y Clases de `com.example.proyecto_appmovil` (`di`, `data.local`, `data.remote`, `data.repository`, `model`, `navigation`, `ui.screens`, `ui.components`, `ui.theme`, `util`).
3. **[03_diagrama_uml_componentes.md](./03_diagrama_uml_componentes.md)**: Diagrama UML de Componentes y secuencias transaccionales (`InscripcionesApp -> MainActivity -> AppNavHost -> Compose Screens -> ViewModels (StateFlow) -> InscripcionesRepository -> [Room AppDatabase | Retrofit ApiService]`).
4. **[04_diagrama_uml_despliegue.md](./04_diagrama_uml_despliegue.md)**: Diagrama UML de Despliegue físico (Dispositivo Android, Protocolo REST JSON/`multipart/form-data`, Servidor Node.js/Express con Multer/JWT/Helmet, MySQL 8 InnoDB y sistema de archivos `/uploads`).

---

## 2. Matriz de Cumplimiento de Transacciones CRUD (Room + Retrofit > 85% del Negocio)

| Operación CRUD | Caso de Uso de Negocio | Endpoint Web API (Retrofit) | Operación en Base de Datos Local (SQLite-Room) | Pantalla / ViewModel | Estado |
| :---: | :--- | :--- | :--- | :--- | :---: |
| **AGREGAR (Create)** | Registrar nuevo aspirante | `POST /api/v1/auth/register` (`201 Created`) | `@Transaction AspiranteDao.guardarSesionRegistro(UsuarioEntity, AspiranteEntity)` | `RegisterScreen` / `RegisterViewModel` | **100% Activo** |
| **AGREGAR (Create)** | Cargar documento PDF / multimedia con copia interna y SHA-256 | `@Multipart POST /api/v1/aspirantes/me/documentos` (`201 Created`) | `DocumentoDao.insert(DocumentoEntity)` con `hash_sha256` y estado `'pendiente'` (`SUBIDO`) | `DocumentsScreen` / `DocumentsViewModel` | **100% Activo** |
| **AGREGAR (Create)** | Registrar comprobante de pago | `POST /api/v1/aspirantes/me/pago` (`201 Created`) | `ComprobantePagoDao.insertOrUpdate(ComprobantePagoEntity)` + aviso en `NotificacionDao` | `PaymentScreen` / `PaymentViewModel` | **100% Activo** |
| **EDITAR (Update)** | Actualizar datos personales del aspirante | `PUT /api/v1/aspirantes/me` (`200 OK`) | `AspiranteDao.updateDatosPersonales(...)` con actualización reactiva por `Flow` | `ProfileScreen` / `ProfileViewModel` | **100% Activo** |
| **EDITAR (Update)** | Marcar notificación como leída y recalcular badge | `PATCH /api/v1/notificaciones/{id}/leida` (`200 OK`) | `NotificacionDao.marcarLeida(id)` (`leida = 1`) + recálculo de `countNoLeidasFlow()` | `NotificationsScreen` / `NotificationsViewModel` | **100% Activo** |
| **EDITAR (Update)** | Revisión y dictamen de Control Escolar | `POST /api/v1/admin/documentos/{id}/revision` (`200 OK`) | `DocumentoDao.updateDictamenRevision(...)` + `AspiranteDao.updateEstadoInscripcion(...)` + `NotificacionDao.insertNotificacion(...)` | `AdminHomeScreen` / `AdminHomeViewModel` | **100% Activo** |
| **VER / LISTAR (Read)** | Listado reactivo de documentos vigentes | `GET /api/v1/aspirantes/me/documentos` (`200 OK`) | `DocumentoDao.getDocumentosFlow()` (última versión por tipo de documento) | `DocumentsScreen` / `StatusScreen` | **100% Activo** |
| **VER / LISTAR (Read)** | Listado de notificaciones y fechas importantes | `GET /api/v1/notificaciones` y `GET /api/v1/fechas-importantes` (`200 OK`) | `NotificacionDao.getNotificacionesFlow()` y `FechaImportanteDao.getFechasVigentesFlow()` | `NotificationsScreen` / `NotificationsViewModel` | **100% Activo** |
| **VER / LISTAR (Read)** | Dashboard agregado del aspirante | `GET /api/v1/aspirantes/me/resumen` (`200 OK`) | Consulta combinada `combine(aspiranteFlow, documentosFlow, countNoLeidasFlow, fechasFlow)` | `HomeScreen` / `HomeViewModel` | **100% Activo** |
| **ELIMINAR (Delete)** | Eliminar documento no aprobado | `DELETE /api/v1/documentos/{id}` (`200 OK` / `409 Conflict` si aprobado) | `DocumentoDao.deleteById(id)` + borrado de copia en almacenamiento interno | `DocumentsScreen` / `DocumentsViewModel` | **100% Activo** |
| **ELIMINAR (Delete)** | Eliminar notificación | `DELETE /api/v1/notificaciones/{id}` (`200 OK`) | `NotificacionDao.eliminar(id)` | `NotificationsScreen` / `NotificationsViewModel` | **100% Activo** |
| **RECUPERAR (Restore)** | Recuperación de sesión y refresh automático de JWT | `POST /api/v1/auth/refresh` (`200 OK`) | Validación en `SessionStore` (DataStore) + lectura offline de `AspiranteDao.getAspiranteSync()` | `LoginScreen` / `LoginViewModel` | **100% Activo** |
| **RECUPERAR (Restore)** | Sincronización de catálogos para uso offline | `GET /api/v1/catalogos/carreras`, `/estados`, `/municipios` (`200 OK`) | `CatalogosDao.insertCarreras()`, `insertEstados()`, `insertMunicipios()` | `RegisterScreen` / `RegisterViewModel` | **100% Activo** |


---

## 3. Correcciones y Ajustes del Segundo Entregable (`feature/correcciones2doEntregable`)

En esta iteración se consolidaron las correcciones prioritarias solicitadas para el segundo entregable:

### 3.1 Sustitución de Colores Rosas y Purples por la Paleta Oficial Azul y Gris
Se eliminó la gama previa en tonos ciruela, vino y rosas pastel (`#874B6D`, `#6B2D52`, `#FFD9EA`, `#EFDDE0`, etc.) y se implementaron en `Color.kt` y `colors.xml` los 9 valores hexadecimales seleccionados:

| Código Hex | Nombre Asignado | Uso en la Aplicación |
| :---: | :--- | :--- |
| **`#ACD0F2`** | `ColorAzulCielo` (`ItsurRosaPastel`) | Contenedor principal, banners de bienvenida, cards destacadas. |
| **`#A7C7E4`** | `ColorAzulPastel` (`ItsurRosaSuave`) | Tarjetas de opción del menú principal, tarjetas secundarias. |
| **`#A2BDD7`** | `ColorAzulAcero` | Superficies suaves, contenedores terciarios y bordes sutiles. |
| **`#9EB4CA`** | `ColorGrisAzulado1` | Chips informativos, variantes de superficie e indicadores. |
| **`#99ABBE`** | `ColorGrisAzulado2` | Acentos de interfaz y botones secundarios. |
| **`#94A3B1`** | `ColorPizarra` | Contornos y elementos de énfasis gráfico. |
| **`#8A9198`** | `ColorGrisFrio` (`ItsurOutline`) | Bordes de `OutlinedTextField`, divisores y líneas de separación. |
| **`#85898C`** | `ColorGrisMedio` (`ItsurOnSurfaceVariant`) | Texto secundario institucional, metadatos y subtítulos. |
| **`#808080`** | `ColorGrisNeutro` | Íconos inactivos y estados neutros. |

* **Color Primario Institucional:** `#1E3A5F` (Azul marino profundo de alto contraste para TopBars y botones principales con texto blanco, cumpliendo norma WCAG AAA).
* **Color de Títulos:** `#112233` (Azul pizarra oscuro para encabezados y textos prominentes).

### 3.2 Corrección UI/UX del Teclado en Campos de Contraseña
* **Problema:** Al desplegarse el teclado virtual (IME), los campos de contraseña en `LoginScreen` y `RegisterScreen` quedaban ocultos debajo del teclado.
* **Solución Implementada:**
  1. Integración de `BringIntoViewRequester` y `onFocusChanged` dentro de `ItsurTextField`: cada vez que un campo de contraseña recibe el foco del usuario, solicita programáticamente a Compose desplazarse para quedar en la porción visible de la pantalla.
  2. Adición de `Modifier.imePadding()` en `Scaffold` y contenedores desplazables (`verticalScroll`), permitiendo que el área de scroll se reduzca dinámicamente según la altura exacta del teclado virtual.
  3. Soporte de acciones de teclado `ImeAction.Done` y `KeyboardActions` para ejecutar el login o registro de forma fluida.

### 3.3 Eliminación de Opciones de Fotografías Digital y Firma de Reglamento
* Se retiraron las tarjetas y accesos a **"Foto para Credencial"** y **"Reglamento Institucional"** del `HomeScreen` del aspirante.
* Se removieron las rutas `CredentialPhoto` y `Reglamento` del flujo activo de navegación en `AppNavHost.kt`.
* Se limpió el enum `TipoDocumento`, eliminando `FOTO_INFANTIL` y `REGLAMENTO_FIRMADO` para centrar la carga únicamente en los documentos académicos oficiales requeridos (Acta de Nacimiento, CURP, Certificado de Bachillerato y Comprobante de Domicilio).

### 3.4 Eliminación de Partes Hardcodeadas y Persistencia Real en Node.js + MySQL 8
* **Base de datos MySQL (`bd_inscripciones_itsur`):**
  - Módulo `server/src/db.js` con pool de conexiones `mysql2/promise` e inicialización automática de catálogos (`carreras`, `estados`, `municipios`, `tipos_documento`, usuario administrador de control escolar y aspirante demo).
* **API REST (`server/src/app.js`):**
  - `POST /api/v1/auth/register`: Transacción completa en MySQL que valida duplicidad por correo o CURP, aplica hash con `bcrypt` a la contraseña, inserta en la tabla `usuarios`, genera el folio oficial (`ITSUR-2026-XXXXX`), e inserta en la tabla `aspirantes` vinculando carrera, estado y municipio reales.
  - `POST /api/v1/auth/login`: Consulta directa a la tabla `usuarios` y `aspirantes` en MySQL con verificación segura de contraseñas.
  - `GET /api/v1/aspirantes/me` y `PUT /api/v1/aspirantes/me`: Lectura y actualización sincronizadas en MySQL.
  - `GET /api/v1/catalogos/*`: Catálogos leídos directamente de las tablas normalizadas de MySQL.
  - `GET /api/v1/admin/aspirantes`: Listado real de aspirantes registrados obtenido mediante `JOIN` entre `aspirantes`, `usuarios` y `carreras` en MySQL.
* **Cliente Android (`InscripcionesRepositoryImpl`):**
  - Se eliminaron las asignaciones locales ficticias que simulaban folios y éxito sin conexión al servidor.
  - El registro envía el payload completo (`RegisterRequestDto`) con carrera, CURP, teléfono, correo y contraseña a la Web API, persistiendo tanto en MySQL como en la base de datos local SQLite-Room.
