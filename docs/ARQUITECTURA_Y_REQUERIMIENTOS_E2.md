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
