# C) Diagrama UML de Componentes y Cadena de Dependencias

**Proyecto:** Sistema de Inscripciones ITSUR (Nuevo Ingreso)  
**Flujo Arquitectónico Obligatorio:**  
`Application (InscripcionesApp) -> Activity (MainActivity) -> Navigation (AppNavHost) -> Compose Screens -> ViewModels (StateFlow) -> Repository (InscripcionesRepository) -> [LocalDataSource (Room AppDatabase / DAOs) | RemoteDataSource (Retrofit ApiService / OkHttp)]`

---

## 1. Diagrama UML de Componentes y Flujo Reactivo Unidireccional (UDF)

```mermaid
flowchart TD
    subgraph ANDROID_APP["Contenedor de Aplicación Android (InscripcionesApp)"]
        APP["«Application»\nInscripcionesApp\nInicializa DefaultAppContainer(this)"]
        DI["«DI Container»\nDefaultAppContainer\nSingleton: AppDatabase, SessionStore,\nRetrofitClient, InscripcionesRepositoryImpl"]
    end

    subgraph PRESENTATION["Capa de Presentación y Navegación (UI Layer)"]
        ACT["«ComponentActivity»\nMainActivity\nsetContent + Proyecto_AppMovilTheme"]
        NAV["«Navigation»\nAppNavHost + Screen\nControl de rutas por rol (aspirante / escolar)\ny limpieza de backstack popUpTo"]
        SCREENS["«Jetpack Compose Screens»\n• LoginScreen / RegisterScreen\n• HomeScreen (Dashboard)\n• DocumentsScreen (SAF + SHA-256)\n• ProfileScreen / StatusScreen\n• NotificationsScreen / PaymentScreen\n• AdminHomeScreen / AdminFechasScreen"]
        VMS["«AndroidX ViewModels»\nStateFlow<UiState> reactivo\nInyectados vía AppViewModelProvider.Factory"]
    end

    subgraph DOMAIN_DATA["Capa de Repositorio (Single Source of Truth)"]
        REPO["«Repository»\nInscripcionesRepository\nInscripcionesRepositoryImpl\nCoordina transacciones locales y remotas"]
        HASH_UTIL["«Utility»\nFileHashHelper\nCopia a filesDir/documentos\ny cálculo SHA-256"]
    end

    subgraph LOCAL_DS["LocalDataSource (Persistencia Offline-First)"]
        ROOM["«RoomDatabase»\nAppDatabase (bd_inscripciones_itsur.db)\n+ RoomConverters"]
        DAOS["«Room DAOs (Flow & Suspend)»\n• AspiranteDao\n• DocumentoDao\n• ComprobantePagoDao\n• NotificacionDao\n• FechaImportanteDao\n• CatalogosDao"]
        DATASTORE["«DataStore Preferences»\nSessionStore\n(accessToken, refreshToken, expiración, rol)"]
    end

    subgraph REMOTE_DS["RemoteDataSource (Cliente HTTP / Web API)"]
        RETROFIT["«Retrofit 2 + Gson»\nInscripcionesApiService\nBASE_URL: http://10.0.2.2:3000/api/v1/"]
        OKHTTP["«OkHttpClient»\n• AuthInterceptor (Bearer Token)\n• HttpLoggingInterceptor\n• FallbackMockServerInterceptor"]
    end

    APP -->|1. Instancia al arrancar| DI
    ACT -->|2. Monta grafo Compose| NAV
    NAV -->|3. Renderiza destino activo| SCREENS
    SCREENS <-->|4. Observa StateFlow / Envía Eventos| VMS
    DI -.->|Inyecta repositorio vía Factory| VMS
    VMS -->|5. Invoca casos de negocio suspend/Flow| REPO
    REPO -->|Copia archivos y calcula hash| HASH_UTIL
    REPO <-->|6a. Transacciones SQLite y Flows reactivos| DAOS
    DAOS <--> ROOM
    REPO <-->|Persistencia de JWT y Refresh| DATASTORE
    REPO <-->|6b. Peticiones HTTP JSON y @Multipart| RETROFIT
    RETROFIT --> OKHTTP
```

---

## 2. Diagramas de Secuencia de las Transacciones CRUD Implementadas

### 2.1 AGREGAR (Create): Registro de Aspirante y Carga de Documento con SHA-256

```mermaid
sequenceDiagram
    autonumber
    actor U as Aspirante
    participant UI as DocumentsScreen / RegisterScreen
    participant VM as ViewModel (StateFlow)
    participant R as InscripcionesRepositoryImpl
    participant FH as FileHashHelper
    participant API as InscripcionesApiService (Retrofit)
    participant DAO as Room DAOs (AppDatabase)

    Note over U,DAO: Transacción 1: Registrar Nuevo Aspirante (POST /auth/register)
    U->>UI: Captura formulario y pulsa "Registrarse"
    UI->>VM: registrarAspirante()
    VM->>R: registrarAspirante(aspirante, contrasena)
    R->>API: POST /api/v1/auth/register (RegisterRequestDto)
    API-->>R: 201 Created (LoginResponseDto: JWT + usuario + aspirante)
    R->>DAO: @Transaction guardarSesionRegistro(UsuarioEntity, AspiranteEntity)
    R->>DAO: insertNotificacion(Bienvenida con Folio)
    DAO-->>VM: Flow actualizado
    VM-->>UI: Navega a HomeScreen

    Note over U,DAO: Transacción 2: Cargar Documento PDF / Multimedia (@Multipart)
    U->>UI: Selecciona PDF (OpenDocument SAF)
    UI->>VM: subirDocumentoSeleccionado(tipo, uri)
    VM->>R: subirDocumento(documento)
    R->>FH: copyToInternalAndComputeSha256(context, uri)
    FH-->>R: InternalFileInfo (file, sizeBytes, sha256Hex)
    R->>DAO: documentoDao.insert(DocumentoEntity estado='pendiente' / SUBIDO)
    R->>API: @Multipart POST /api/v1/aspirantes/me/documentos (idTipo, sha256, archivo)
    API-->>R: 201 Created (SubirDocumentoResponseDto)
    R->>DAO: documentoDao.update(idRemoto, syncPendiente=false)
    R->>DAO: aspiranteDao.updateEstadoInscripcion('en_revision')
    DAO-->>VM: DocumentoDao.getDocumentosFlow() emite nueva lista
    VM-->>UI: Actualiza StatusChip a "Subido (En revisión)"
```

### 2.2 EDITAR, ELIMINAR Y RECUPERAR (Update, Delete, Restore)

```mermaid
sequenceDiagram
    autonumber
    actor U as Usuario (Aspirante / Escolar)
    participant VM as ViewModel
    participant R as InscripcionesRepositoryImpl
    participant SS as SessionStore (DataStore)
    participant API as InscripcionesApiService (Retrofit)
    participant DAO as Room DAOs (AppDatabase)

    Note over U,DAO: RECUPERAR: Validación y Refresh de Sesión + Caché Offline
    U->>VM: Abre la aplicación (LoginViewModel.init)
    VM->>R: recuperarSesionActiva()
    R->>SS: getSession()
    alt Token Expirado
        R->>API: POST /api/v1/auth/refresh (refreshToken)
        API-->>R: 200 OK (Nuevo accessToken + refreshToken)
        R->>SS: updateTokens()
    end
    R->>DAO: aspiranteDao.getAspiranteSync() (Sin requerir red)
    DAO-->>VM: Aspirante recuperado -> Navega directo a Home

    Note over U,DAO: EDITAR: Marcar Notificación Leída y Recalcular Badge
    U->>VM: Toca notificación en NotificationsScreen
    VM->>R: marcarNotificacionLeida(id)
    R->>DAO: notificacionDao.marcarLeida(id) (leida = 1)
    R->>API: PATCH /api/v1/notificaciones/{id}/leida
    DAO-->>VM: countNoLeidasFlow() recalcula badge en HomeScreen

    Note over U,DAO: EDITAR: Revisión de Control Escolar (Dictamen)
    U->>VM: Control Escolar dictamina documento (Aprobar / Rechazar)
    VM->>R: revisarDocumentoControlEscolar(idDoc, resultado, observaciones)
    R->>API: POST /api/v1/admin/documentos/{id}/revision
    API-->>R: 200 OK (RevisionResponseDto + notificacionGenerada)
    R->>DAO: documentoDao.updateDictamenRevision(idDoc, resultado, obs)
    R->>DAO: aspiranteDao.updateEstadoInscripcion(nuevoEstado)
    R->>DAO: notificacionDao.insertNotificacion(alertaDictamen)

    Note over U,DAO: ELIMINAR: Borrado de Documento no aprobado y Notificación
    U->>VM: Pulsa eliminar documento pendiente/rechazado
    VM->>R: eliminarDocumento(idDoc)
    R->>API: DELETE /api/v1/documentos/{idDoc}
    API-->>R: 200 OK
    R->>DAO: documentoDao.deleteById(idDoc)
```
