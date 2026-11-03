package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.data.local.dao.*
import com.example.proyecto_appmovil.data.local.entity.*
import com.example.proyecto_appmovil.data.local.session.SessionManager
import com.example.proyecto_appmovil.data.remote.api.InscripcionesApiService
import com.example.proyecto_appmovil.data.remote.dto.*
import com.example.proyecto_appmovil.model.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.time.LocalDate
import java.time.temporal.ChronoUnit

class InscripcionesRepositoryImpl(
    private val aspiranteDao: AspiranteDao,
    private val documentoDao: DocumentoDao,
    private val comprobantePagoDao: ComprobantePagoDao,
    private val notificacionDao: NotificacionDao,
    private val fechaImportanteDao: FechaImportanteDao,
    private val catalogosDao: CatalogosDao,
    private val usuarioDao: UsuarioDao,
    private val apiService: InscripcionesApiService,
    private val sessionManager: SessionManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : InscripcionesRepository {

    // ==========================================
    // --- DATOS DE DEMOSTRACIÓN (FALLBACK OFFLINE) ---
    // Se usan cuando la Web API no está disponible y Room aún no tiene datos,
    // para que las pantallas Compose sigan siendo navegables.
    // ==========================================

    private var currentAspirante = Aspirante(
        id = "1",
        nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
        curp = "OOAD030515HGTRRN01",
        email = "aspirante@itsur.edu.mx",
        telefono = "4451234567",
        carrera = "Ing. Sistemas Computacionales",
        folioAspirante = "ITSUR-2026-0042",
        promedioBachillerato = 9.2,
        direccion = "Calle Hidalgo #123, Uriangato, Gto.",
        estadoInscripcion = EstadoInscripcion.EN_REVISION,
        rol = "aspirante"
    )

    private val documentosList = mutableListOf(
        Documento(
            id = "doc_1",
            tipo = TipoDocumento.ACTA_NACIMIENTO,
            nombreArchivo = "Acta_Nacimiento_Dafne.pdf",
            uriString = "content://media/external/file/101",
            tamanoBytes = 1420500,
            fechaSubida = "01/11/2026 10:30",
            estado = EstadoDocumento.VALIDADO,
            observaciones = "Documento verificado correctamente."
        ),
        Documento(
            id = "doc_2",
            tipo = TipoDocumento.CURP,
            nombreArchivo = "CURP_Certificada.pdf",
            uriString = "content://media/external/file/102",
            tamanoBytes = 850200,
            fechaSubida = "01/11/2026 10:32",
            estado = EstadoDocumento.VALIDADO,
            observaciones = "CURP actualizada ante RENAPO."
        ),
        Documento(
            id = "doc_3",
            tipo = TipoDocumento.CERTIFICADO_BACHILLERATO,
            nombreArchivo = "Certificado_Preparatoria.pdf",
            uriString = "content://media/external/file/103",
            tamanoBytes = 2950000,
            fechaSubida = "02/11/2026 14:15",
            estado = EstadoDocumento.SUBIDO,
            observaciones = "Pendiente de cotejo por Servicios Escolares."
        ),
        Documento(
            id = "doc_4",
            tipo = TipoDocumento.COMPROBANTE_DOMICILIO,
            nombreArchivo = null,
            uriString = null,
            tamanoBytes = 0,
            fechaSubida = null,
            estado = EstadoDocumento.PENDIENTE,
            observaciones = "Recibo de luz, agua o teléfono no mayor a 3 meses."
        )
    )

    private val notificacionesList = mutableListOf(
        Notificacion(
            id = "notif_1",
            titulo = "Documentos en Revisión",
            mensaje = "Tus documentos han sido recibidos por el personal de Servicios Escolares del ITSUR y están en proceso de verificación.",
            fecha = "02 de Noviembre, 2026",
            leida = false
        ),
        Notificacion(
            id = "notif_2",
            titulo = "Examen Diagnóstico de Admisión",
            mensaje = "El examen diagnóstico de nuevo ingreso se aplicará en línea el día 15 de Noviembre a las 09:00 hrs.",
            fecha = "30 de Octubre, 2026",
            leida = false
        ),
        Notificacion(
            id = "notif_3",
            titulo = "Registro Exitoso",
            mensaje = "Bienvenido al proceso de admisión ITSUR 2026. Tu número de folio asignado es ITSUR-2026-0042.",
            fecha = "28 de Octubre, 2026",
            leida = true
        )
    )

    private val fechasList = listOf(
        FechaImportante(
            id = "f_1",
            titulo = "Cierre de Carga de Documentos",
            descripcion = "Fecha límite para subir Acta, CURP y Certificado al sistema.",
            fechaLimite = "10 de Noviembre, 2026",
            diasRestantes = 8,
            tipo = "limite_entrega"
        ),
        FechaImportante(
            id = "f_2",
            titulo = "Examen de Admisión / Diagnóstico",
            descripcion = "Aplicación del examen CENEVAL en instalaciones ITSUR y virtual.",
            fechaLimite = "15 de Noviembre, 2026",
            diasRestantes = 13,
            tipo = "examen"
        ),
        FechaImportante(
            id = "f_3",
            titulo = "Publicación de Resultados",
            descripcion = "Consulta de listas de aspirantes aceptados en el portal oficial.",
            fechaLimite = "25 de Noviembre, 2026",
            diasRestantes = 23,
            tipo = "publicacion"
        ),
        FechaImportante(
            id = "f_4",
            titulo = "Pago de Cuota de Inscripción",
            descripcion = "Pago bancario de la cuota semestral de nuevo ingreso.",
            fechaLimite = "05 de Diciembre, 2026",
            diasRestantes = 33,
            tipo = "pago"
        )
    )

    private var comprobantePago = ComprobantePago(
        id = "pago_1",
        aspiranteId = "1",
        folio = "BBVA-84920193",
        monto = 2850.0,
        fechaPago = "02/11/2026",
        comprobanteUri = "",
        validado = false
    )

    private var reglamentoFirmado = false

    private val aspirantesAdminList = mutableListOf(
        Aspirante(
            id = "1",
            nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
            curp = "OOAD030515HGTRRN01",
            email = "aspirante@itsur.edu.mx",
            telefono = "4451234567",
            carrera = "Ing. Sistemas Computacionales",
            folioAspirante = "ITSUR-2026-0042",
            promedioBachillerato = 9.2,
            direccion = "Uriangato, Gto.",
            estadoInscripcion = EstadoInscripcion.EN_REVISION,
            rol = "aspirante"
        ),
        Aspirante(
            id = "2",
            nombreCompleto = "Alan Yael Fonseca Ruiz",
            curp = "FORA020820HGTRNA09",
            email = "alan@itsur.edu.mx",
            telefono = "4457654321",
            carrera = "Ing. Sistemas Computacionales",
            folioAspirante = "ITSUR-2026-0043",
            promedioBachillerato = 9.5,
            direccion = "Moroleón, Gto.",
            estadoInscripcion = EstadoInscripcion.ACEPTADO,
            rol = "aspirante"
        ),
        Aspirante(
            id = "3",
            nombreCompleto = "Montserrat Tenorio Villagómez",
            curp = "TEVM031110MGTSRR05",
            email = "montserrat@itsur.edu.mx",
            telefono = "4459876543",
            carrera = "Ing. Sistemas Computacionales",
            folioAspirante = "ITSUR-2026-0044",
            promedioBachillerato = 9.0,
            direccion = "Yuriria, Gto.",
            estadoInscripcion = EstadoInscripcion.CON_OBSERVACIONES,
            rol = "aspirante"
        )
    )

    // ==========================================
    // --- AUTENTICACIÓN Y SESIÓN ---
    // ==========================================

    override suspend fun login(usuarioOCorreo: String, contrasena: String): Result<Aspirante> = withContext(ioDispatcher) {
        val email = usuarioOCorreo.trim()
        try {
            val response = apiService.login(LoginRequestDto(email = email, contrasena = contrasena))

            // Guardar sesión segura con EncryptedSharedPreferences
            sessionManager.saveSession(
                token = response.token,
                refreshToken = response.refreshToken,
                userId = response.usuario.idUsuario,
                aspiranteId = response.aspirante?.idAspirante,
                role = response.usuario.rol,
                email = response.usuario.correo
            )

            // Cachear usuario en Room
            val userEntity = UsuarioEntity(
                idUsuario = response.usuario.idUsuario,
                nombre = response.usuario.nombre,
                apellidos = response.usuario.apellidos,
                nombreUsuario = response.usuario.nombreUsuario,
                correo = response.usuario.correo,
                rol = response.usuario.rol,
                activo = response.usuario.activo
            )
            usuarioDao.insertOrUpdate(userEntity)

            // Cachear aspirante en Room si existe
            response.aspirante?.let { aspDto ->
                val aspiranteEntity = AspiranteEntity(
                    idAspirante = aspDto.idAspirante,
                    idUsuario = aspDto.idUsuario,
                    folio = aspDto.folio,
                    curp = aspDto.curp,
                    telefono = aspDto.telefono,
                    direccion = aspDto.direccion ?: "",
                    idEstado = aspDto.idEstado,
                    idMunicipio = aspDto.idMunicipio,
                    idCarrera = aspDto.idCarrera,
                    promedioBachillerato = aspDto.promedioBachillerato ?: 0.0,
                    estadoInscripcion = aspDto.estadoInscripcion
                )
                aspiranteDao.insertOrUpdate(aspiranteEntity)
            }

            // Descargar catálogos y datos iniciales en segundo plano
            try {
                refreshCatalogos()
                refreshFechasImportantes()
            } catch (_: Exception) { }

            val aspiranteModel = response.aspirante?.let {
                aspiranteDao.getAspiranteSync()?.let { entity -> mapEntityToAspirante(entity, userEntity) }
            } ?: Aspirante(
                id = response.aspirante?.idAspirante?.toString() ?: "1",
                nombreCompleto = "${response.usuario.nombre} ${response.usuario.apellidos}",
                curp = response.aspirante?.curp ?: "",
                email = response.usuario.correo,
                telefono = response.aspirante?.telefono ?: "",
                carrera = response.aspirante?.carreraNombre ?: "Ingeniería en Sistemas Computacionales",
                folioAspirante = response.aspirante?.folio ?: "FOL-2026-001",
                rol = response.usuario.rol
            )

            Result.success(aspiranteModel)
        } catch (e: Exception) {
            // Estrategia Offline: si no hay red pero el usuario ya había iniciado sesión
            val localUser = usuarioDao.getUsuarioSync()
            val localAspirante = aspiranteDao.getAspiranteSync()

            if (localUser != null && localUser.correo.equals(email, ignoreCase = true) && localAspirante != null) {
                val model = mapEntityToAspirante(localAspirante, localUser)
                Result.success(model)
            } else if (e is java.io.IOException) {
                // Modo demostración: la Web API no es alcanzable y no hay caché local.
                Result.success(loginDemo(email))
            } else {
                // Error real del servidor (p. ej. 401 credenciales inválidas)
                Result.failure(e)
            }
        }
    }

    /** Login simulado (sin servidor). Los usuarios con "admin", "escolar" o "control" entran como Control Escolar. */
    private fun loginDemo(usuarioOCorreo: String): Aspirante {
        val userTrim = usuarioOCorreo.trim().lowercase()
        if (userTrim.contains("admin") || userTrim.contains("escolar") || userTrim.contains("control")) {
            return currentAspirante.copy(
                nombreCompleto = "Lic. Gustavo Vega (Control Escolar)",
                email = "escolar@itsur.edu.mx",
                rol = "escolar"
            )
        }
        return currentAspirante
    }

    override suspend fun loginApi(email: String, contrasena: String): Result<LoginResponseDto> = withContext(ioDispatcher) {
        try {
            val response = apiService.login(LoginRequestDto(email, contrasena))
            sessionManager.saveSession(
                token = response.token,
                refreshToken = response.refreshToken,
                userId = response.usuario.idUsuario,
                aspiranteId = response.aspirante?.idAspirante,
                role = response.usuario.rol,
                email = response.usuario.correo
            )
            usuarioDao.insertOrUpdate(
                UsuarioEntity(
                    idUsuario = response.usuario.idUsuario,
                    nombre = response.usuario.nombre,
                    apellidos = response.usuario.apellidos,
                    nombreUsuario = response.usuario.nombreUsuario,
                    correo = response.usuario.correo,
                    rol = response.usuario.rol,
                    activo = response.usuario.activo
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registrarAspirante(aspirante: Aspirante, contrasena: String): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val idCarrera = when {
                aspirante.carrera.contains("Automotriz", ignoreCase = true) -> 2
                aspirante.carrera.contains("Ambiental", ignoreCase = true) -> 3
                aspirante.carrera.contains("Gestión", ignoreCase = true) -> 4
                aspirante.carrera.contains("Industrial", ignoreCase = true) -> 5
                aspirante.carrera.contains("Electrónica", ignoreCase = true) -> 6
                aspirante.carrera.contains("Semiconductores", ignoreCase = true) -> 7
                aspirante.carrera.contains("Gastronomía", ignoreCase = true) -> 8
                else -> 1
            }

            val request = RegisterRequestDto(
                nombre = aspirante.nombreCompleto.substringBeforeLast(" ", aspirante.nombreCompleto),
                apellidos = aspirante.nombreCompleto.substringAfterLast(" ", ""),
                curp = aspirante.curp,
                correo = aspirante.email,
                telefono = aspirante.telefono,
                idCarrera = idCarrera,
                contrasena = contrasena,
                direccion = aspirante.direccion,
                idEstado = 11,
                idMunicipio = 1
            )
            val response = apiService.register(request)
            sessionManager.saveSession(
                token = response.token,
                refreshToken = response.refreshToken,
                userId = response.usuario.idUsuario,
                aspiranteId = response.aspirante?.idAspirante,
                role = response.usuario.rol,
                email = response.usuario.correo
            )

            // Persistir usuario en Room
            val userEntity = UsuarioEntity(
                idUsuario = response.usuario.idUsuario,
                nombre = response.usuario.nombre,
                apellidos = response.usuario.apellidos,
                nombreUsuario = response.usuario.nombreUsuario,
                correo = response.usuario.correo,
                rol = response.usuario.rol,
                activo = response.usuario.activo
            )
            usuarioDao.insertOrUpdate(userEntity)

            // Persistir aspirante retornado de MySQL en Room
            response.aspirante?.let { aspDto ->
                val aspiranteEntity = AspiranteEntity(
                    idAspirante = aspDto.idAspirante,
                    idUsuario = aspDto.idUsuario,
                    folio = aspDto.folio,
                    curp = aspDto.curp,
                    telefono = aspDto.telefono,
                    direccion = aspDto.direccion ?: "",
                    idEstado = aspDto.idEstado,
                    idMunicipio = aspDto.idMunicipio,
                    idCarrera = aspDto.idCarrera,
                    promedioBachillerato = aspDto.promedioBachillerato ?: 9.0,
                    estadoInscripcion = aspDto.estadoInscripcion
                )
                aspiranteDao.insertOrUpdate(aspiranteEntity)
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerApi(request: RegisterRequestDto): Result<LoginResponseDto> = withContext(ioDispatcher) {
        try {
            val response = apiService.register(request)
            sessionManager.saveSession(
                token = response.token,
                userId = response.usuario.idUsuario,
                aspiranteId = response.aspirante?.idAspirante,
                role = response.usuario.rol,
                email = response.usuario.correo
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(ioDispatcher) {
        try {
            try {
                apiService.logout()
            } catch (_: Exception) { }
            sessionManager.clearSession()
            usuarioDao.clearAll()
            aspiranteDao.clearAll()
            documentoDao.clearAll()
            comprobantePagoDao.clearAll()
            notificacionDao.clearAll()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    override fun getUsuarioActualFlow(): Flow<UsuarioEntity?> = usuarioDao.getUsuarioFlow()

    override suspend fun getUsuarioActualSync(): UsuarioEntity? = withContext(ioDispatcher) {
        usuarioDao.getUsuarioSync()
    }

    // ==========================================
    // --- PERFIL DE ASPIRANTE & DASHBOARD ---
    // ==========================================

    override fun getAspiranteFlow(): Flow<AspiranteEntity?> = aspiranteDao.getAspiranteFlow()

    override suspend fun getAspiranteActual(): Aspirante = withContext(ioDispatcher) {
        val entity = aspiranteDao.getAspiranteSync()
        val user = usuarioDao.getUsuarioSync()
        if (entity != null && user != null) {
            mapEntityToAspirante(entity, user)
        } else {
            currentAspirante
        }
    }

    override suspend fun getAspiranteSync(): AspiranteEntity? = withContext(ioDispatcher) {
        aspiranteDao.getAspiranteSync()
    }

    override suspend fun refreshAspirante(): Result<AspiranteEntity> = withContext(ioDispatcher) {
        try {
            val dto = apiService.getMiAspirante()
            val entity = AspiranteEntity(
                idAspirante = dto.idAspirante,
                idUsuario = dto.idUsuario,
                folio = dto.folio,
                curp = dto.curp,
                telefono = dto.telefono,
                direccion = dto.direccion ?: "",
                idEstado = dto.idEstado,
                idMunicipio = dto.idMunicipio,
                idCarrera = dto.idCarrera,
                promedioBachillerato = dto.promedioBachillerato ?: 0.0,
                estadoInscripcion = dto.estadoInscripcion
            )
            aspiranteDao.insertOrUpdate(entity)
            Result.success(entity)
        } catch (e: Exception) {
            val local = aspiranteDao.getAspiranteSync()
            if (local != null) Result.success(local) else Result.failure(e)
        }
    }

    override suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val idAspirante = aspirante.id.toLongOrNull() ?: sessionManager.getAspiranteId()
            aspiranteDao.updateDatosPersonales(
                idAspirante = idAspirante,
                telefono = aspirante.telefono,
                direccion = aspirante.direccion,
                idEstado = null,
                idMunicipio = null
            )
            val body = mapOf(
                "telefono" to aspirante.telefono,
                "direccion" to aspirante.direccion
            )
            try {
                apiService.actualizarMiAspirante(body)
            } catch (_: Exception) { }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarDatosPersonales(
        telefono: String,
        direccion: String,
        idEstado: Int?,
        idMunicipio: Int?
    ): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val idAspirante = sessionManager.getAspiranteId()
            aspiranteDao.updateDatosPersonales(
                idAspirante = idAspirante,
                telefono = telefono,
                direccion = direccion,
                idEstado = idEstado,
                idMunicipio = idMunicipio
            )
            val body = mapOf(
                "telefono" to telefono,
                "direccion" to direccion,
                "idEstado" to idEstado,
                "idMunicipio" to idMunicipio
            )
            try {
                apiService.actualizarMiAspirante(body)
            } catch (_: Exception) { }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getEstatusInscripcion(): Result<Map<String, String>> = withContext(ioDispatcher) {
        try {
            val estatus = apiService.getMiEstatus()
            Result.success(estatus)
        } catch (e: Exception) {
            val local = aspiranteDao.getAspiranteSync()
            if (local != null) {
                Result.success(mapOf("estado" to local.estadoInscripcion, "folio" to local.folio))
            } else {
                Result.failure(e)
            }
        }
    }

    // ==========================================
    // --- DOCUMENTOS (MULTIMEDIA) ---
    // ==========================================

    override fun getDocumentosFlow(): Flow<List<DocumentoEntity>> = documentoDao.getDocumentosFlow()

    override fun getDocumentoPorTipoFlow(tipoCodigo: String): Flow<DocumentoEntity?> =
        documentoDao.getDocumentoPorTipoFlow(tipoCodigo)

    override suspend fun getDocumentos(): List<Documento> = withContext(ioDispatcher) {
        try {
            refreshDocumentos()
            sincronizarDocumentosPendientes()
        } catch (_: Exception) { }

        // Room es la fuente única de verdad; los tipos sin registro local
        // se completan con los datos de demostración.
        val locales = documentoDao.getDocumentosFlow().first()
            .mapNotNull { mapEntityToDocumento(it) }
            .associateBy { it.tipo }
        val base = documentosList.associateBy { it.tipo }
        (base.keys + locales.keys).distinct()
            .sortedBy { it.ordinal }
            .mapNotNull { tipo -> locales[tipo] ?: base[tipo] }
    }

    override suspend fun subirDocumento(
        tipo: TipoDocumento,
        nombreArchivo: String,
        uriString: String,
        tamanoBytes: Long
    ): Result<Boolean> {
        val nuevoDoc = Documento(
            id = "doc_${tipo.name.lowercase()}",
            tipo = tipo,
            nombreArchivo = nombreArchivo,
            uriString = uriString,
            tamanoBytes = tamanoBytes,
            fechaSubida = "Hoy",
            estado = EstadoDocumento.SUBIDO,
            observaciones = "Documento cargado correctamente, en espera de revisión."
        )
        val index = documentosList.indexOfFirst { it.tipo == tipo }
        if (index != -1) documentosList[index] = nuevoDoc else documentosList.add(nuevoDoc)
        // Persistir en Room (syncPendiente = true) para sincronizar con la Web API
        return subirDocumento(nuevoDoc)
    }

    override suspend fun refreshDocumentos(): Result<List<DocumentoEntity>> = withContext(ioDispatcher) {
        try {
            val dtos = apiService.getDocumentos()
            val aspiranteId = sessionManager.getAspiranteId()
            val entities = dtos.map { dto ->
                DocumentoEntity(
                    idRemoto = dto.id,
                    idAspirante = if (dto.idAspirante > 0) dto.idAspirante else aspiranteId,
                    idTipoDocumento = dto.idTipoDocumento,
                    tipoCodigo = dto.tipoCodigo,
                    rutaLocal = dto.url,
                    nombreOriginal = dto.nombreOriginal,
                    tamanoBytes = dto.tamanoBytes,
                    mimeType = dto.mimeType,
                    version = dto.version,
                    estado = dto.estado,
                    observaciones = dto.observaciones,
                    syncPendiente = false
                )
            }
            documentoDao.insertAll(entities)
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subirDocumento(documento: Documento): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val aspiranteId = sessionManager.getAspiranteId()
            val entity = DocumentoEntity(
                idAspirante = aspiranteId,
                idTipoDocumento = documento.tipo.ordinal + 1,
                tipoCodigo = documento.tipo.name,
                rutaLocal = documento.uriString,
                nombreOriginal = documento.nombreArchivo,
                tamanoBytes = documento.tamanoBytes,
                mimeType = "application/pdf",
                version = 1,
                estado = "SUBIDO",
                observaciones = documento.observaciones,
                syncPendiente = true
            )
            documentoDao.insert(entity)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subirDocumentoArchivo(
        tipoCodigo: String,
        idTipoDocumento: Int,
        archivo: File,
        nombreOriginal: String,
        mimeType: String
    ): Result<DocumentoEntity> = withContext(ioDispatcher) {
        val aspiranteId = sessionManager.getAspiranteId()
        var localEntity = DocumentoEntity(
            idAspirante = aspiranteId,
            idTipoDocumento = idTipoDocumento,
            tipoCodigo = tipoCodigo,
            rutaLocal = archivo.absolutePath,
            nombreOriginal = nombreOriginal,
            tamanoBytes = archivo.length(),
            mimeType = mimeType,
            version = 1,
            estado = "SUBIDO",
            syncPendiente = true
        )
        val localId = documentoDao.insert(localEntity)
        localEntity = localEntity.copy(idLocal = localId)

        try {
            val requestFile = archivo.asRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("archivo", nombreOriginal, requestFile)
            val tipoDocBody = idTipoDocumento.toString().toRequestBody("text/plain".toMediaTypeOrNull())
            val codigoBody = tipoCodigo.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.subirDocumento(filePart, tipoDocBody, codigoBody)
            if (response.success && response.documento != null) {
                localEntity = localEntity.copy(
                    idRemoto = response.documento.id,
                    syncPendiente = false,
                    estado = response.documento.estado
                )
                documentoDao.update(localEntity)
            }
            Result.success(localEntity)
        } catch (e: Exception) {
            // Guardado en Room con syncPendiente = true para reintento posterior
            Result.success(localEntity)
        }
    }

    override suspend fun eliminarDocumento(idLocal: Long, idRemoto: Long?): Result<Boolean> = withContext(ioDispatcher) {
        try {
            documentoDao.deleteById(idLocal)
            if (idRemoto != null && idRemoto > 0) {
                try {
                    apiService.eliminarDocumento(idRemoto)
                } catch (_: Exception) { }
            }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sincronizarDocumentosPendientes(): Result<Int> = withContext(ioDispatcher) {
        try {
            val pendientes = documentoDao.getPendientesSync()
            var count = 0
            for (doc in pendientes) {
                val filePath = doc.rutaLocal ?: continue
                val file = File(filePath)
                if (file.exists()) {
                    try {
                        val requestFile = file.asRequestBody((doc.mimeType ?: "application/pdf").toMediaTypeOrNull())
                        val filePart = MultipartBody.Part.createFormData("archivo", doc.nombreOriginal ?: file.name, requestFile)
                        val tipoDocBody = doc.idTipoDocumento.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                        val codigoBody = doc.tipoCodigo.toRequestBody("text/plain".toMediaTypeOrNull())

                        val response = apiService.subirDocumento(filePart, tipoDocBody, codigoBody)
                        if (response.success && response.documento != null) {
                            documentoDao.update(
                                doc.copy(
                                    idRemoto = response.documento.id,
                                    syncPendiente = false,
                                    estado = response.documento.estado
                                )
                            )
                            count++
                        }
                    } catch (_: Exception) { }
                }
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // --- COMPROBANTE DE PAGO (MULTIMEDIA) ---
    // ==========================================

    override fun getPagoFlow(): Flow<ComprobantePagoEntity?> = comprobantePagoDao.getPagoFlow()

    override suspend fun getPagoSync(): ComprobantePagoEntity? = withContext(ioDispatcher) {
        comprobantePagoDao.getPagoSync()
    }

    override suspend fun getComprobantePago(): ComprobantePago = withContext(ioDispatcher) {
        val entity = comprobantePagoDao.getPagoSync()
        entity?.let {
            ComprobantePago(
                id = it.idPago.toString(),
                aspiranteId = it.idAspirante.toString(),
                folio = it.folioPago,
                monto = it.monto,
                fechaPago = it.fechaPago,
                comprobanteUri = it.rutaComprobanteLocal ?: "",
                validado = it.estado == "VALIDADO"
            )
        } ?: comprobantePago
    }

    override suspend fun refreshPago(): Result<ComprobantePagoEntity?> = withContext(ioDispatcher) {
        try {
            val dto = apiService.getPago()
            if (dto != null) {
                val entity = ComprobantePagoEntity(
                    idRemoto = dto.idPago,
                    idAspirante = dto.idAspirante,
                    folioPago = dto.folioPago,
                    monto = dto.monto,
                    fechaPago = dto.fechaPago,
                    rutaComprobanteLocal = dto.urlComprobante,
                    estado = dto.estado,
                    observaciones = dto.observaciones
                )
                comprobantePagoDao.insertOrUpdate(entity)
                Result.success(entity)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            val local = comprobantePagoDao.getPagoSync()
            Result.success(local)
        }
    }

    override suspend fun registrarComprobantePago(comprobante: ComprobantePago): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val aspiranteId = comprobante.aspiranteId.toLongOrNull() ?: sessionManager.getAspiranteId()
            val entity = ComprobantePagoEntity(
                idAspirante = aspiranteId,
                folioPago = comprobante.folio,
                monto = comprobante.monto,
                fechaPago = comprobante.fechaPago,
                rutaComprobanteLocal = comprobante.comprobanteUri,
                estado = "EN_REVISION",
                observaciones = null
            )
            comprobantePagoDao.insertOrUpdate(entity)
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun subirComprobantePago(
        folioPago: String,
        monto: Double,
        fechaPago: String,
        archivo: File?,
        rutaLocal: String?
    ): Result<ComprobantePagoEntity> = withContext(ioDispatcher) {
        val aspiranteId = sessionManager.getAspiranteId()
        var entity = ComprobantePagoEntity(
            idAspirante = aspiranteId,
            folioPago = folioPago,
            monto = monto,
            fechaPago = fechaPago,
            rutaComprobanteLocal = rutaLocal ?: archivo?.absolutePath,
            estado = "SUBIDO"
        )
        val id = comprobantePagoDao.insertOrUpdate(entity)
        entity = entity.copy(idPago = id)

        if (archivo != null && archivo.exists()) {
            try {
                val requestFile = archivo.asRequestBody("image/*".toMediaTypeOrNull())
                val filePart = MultipartBody.Part.createFormData("archivo", archivo.name, requestFile)
                val folioBody = folioPago.toRequestBody("text/plain".toMediaTypeOrNull())
                val montoBody = monto.toString().toRequestBody("text/plain".toMediaTypeOrNull())
                val fechaBody = fechaPago.toRequestBody("text/plain".toMediaTypeOrNull())

                val response = apiService.subirPago(filePart, folioBody, montoBody, fechaBody)
                entity = entity.copy(
                    idRemoto = response.idPago,
                    estado = response.estado
                )
                comprobantePagoDao.insertOrUpdate(entity)
            } catch (_: Exception) { }
        }

        Result.success(entity)
    }

    // ==========================================
    // --- NOTIFICACIONES ---
    // ==========================================

    override fun getNotificacionesFlow(soloNoLeidas: Boolean): Flow<List<NotificacionEntity>> =
        notificacionDao.getNotificacionesFlow(soloNoLeidas)

    override fun countNotificacionesNoLeidasFlow(): Flow<Int> =
        notificacionDao.countNoLeidasFlow()

    override suspend fun getNotificaciones(): List<Notificacion> = withContext(ioDispatcher) {
        try {
            refreshNotificaciones()
        } catch (_: Exception) { }
        val locales = notificacionDao.getNotificacionesFlow(false).first().map { entity ->
            Notificacion(
                id = (entity.idRemoto ?: entity.idNotificacion).toString(),
                titulo = entity.titulo,
                mensaje = entity.mensaje,
                fecha = entity.fechaEnvio,
                leida = entity.leida
            )
        }
        locales.ifEmpty { notificacionesList.toList() }
    }

    override suspend fun refreshNotificaciones(): Result<List<NotificacionEntity>> = withContext(ioDispatcher) {
        try {
            val dtos = apiService.getNotificaciones()
            val userId = sessionManager.getUserId()
            val entities = dtos.map { dto ->
                NotificacionEntity(
                    idRemoto = dto.id,
                    idUsuario = if (dto.idUsuario > 0) dto.idUsuario else userId,
                    tipo = dto.tipo,
                    titulo = dto.titulo,
                    mensaje = dto.mensaje,
                    leida = dto.leida,
                    fechaEnvio = dto.fechaEnvio
                )
            }
            notificacionDao.insertAll(entities)
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun marcarNotificacionLeida(id: Long): Result<Boolean> = withContext(ioDispatcher) {
        try {
            notificacionDao.marcarLeida(id)
            try {
                apiService.marcarNotificacionLeida(id)
            } catch (_: Exception) { }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarNotificacion(id: Long): Result<Boolean> = withContext(ioDispatcher) {
        try {
            notificacionDao.eliminar(id)
            try {
                apiService.eliminarNotificacion(id)
            } catch (_: Exception) { }
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // --- FECHAS IMPORTANTES ---
    // ==========================================

    override fun getFechasImportantesFlow(): Flow<List<FechaImportanteEntity>> =
        fechaImportanteDao.getFechasVigentesFlow()

    override suspend fun refreshFechasImportantes(): Result<List<FechaImportanteEntity>> = withContext(ioDispatcher) {
        try {
            val dtos = apiService.getFechasImportantes()
            val entities = dtos.map { dto ->
                FechaImportanteEntity(
                    idFecha = dto.idFecha,
                    titulo = dto.titulo,
                    descripcion = dto.descripcion,
                    tipo = dto.tipo,
                    fechaLimite = dto.fechaLimite,
                    idCarrera = dto.idCarrera,
                    activa = dto.activa
                )
            }
            fechaImportanteDao.insertAll(entities)
            Result.success(entities)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // --- CATÁLOGOS (OFFLINE-FIRST CACHE) ---
    // ==========================================

    override fun getCarrerasFlow(): Flow<List<CarreraEntity>> = catalogosDao.getCarrerasFlow()

    override fun getEstadosFlow(): Flow<List<EstadoEntity>> = catalogosDao.getEstadosFlow()

    override fun getMunicipiosFlow(idEstado: Int): Flow<List<MunicipioEntity>> =
        catalogosDao.getMunicipiosPorEstadoFlow(idEstado)

    override suspend fun refreshCatalogos(): Result<Boolean> = withContext(ioDispatcher) {
        try {
            val carreras = apiService.getCarreras().map {
                CarreraEntity(it.idCarrera, it.clave, it.nombre, it.area, it.activa)
            }
            catalogosDao.insertCarreras(carreras)

            val estados = apiService.getEstados().map {
                EstadoEntity(it.idEstado, it.nombre, it.abreviatura)
            }
            catalogosDao.insertEstados(estados)

            val municipios = apiService.getMunicipios().map {
                MunicipioEntity(it.idMunicipio, it.idEstado, it.nombre)
            }
            catalogosDao.insertMunicipios(municipios)

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // --- OPERACIONES DE ALTO NIVEL PARA LA UI ---
    // ==========================================

    override suspend fun registrarAspirante(
        nombreCompleto: String,
        curp: String,
        email: String,
        telefono: String,
        carrera: String,
        direccion: String,
        contrasena: String
    ): Result<Boolean> {
        val aspirante = Aspirante(
            id = "0",
            nombreCompleto = nombreCompleto,
            curp = curp.uppercase(),
            email = email,
            telefono = telefono,
            carrera = carrera,
            direccion = direccion,
            folioAspirante = "",
            estadoInscripcion = EstadoInscripcion.PENDIENTE
        )
        return registrarAspirante(aspirante, contrasena)
    }

    override suspend fun registrarComprobantePago(
        folio: String,
        monto: Double,
        fechaPago: String,
        comprobanteUri: String
    ): Result<Boolean> {
        comprobantePago = comprobantePago.copy(
            folio = folio,
            monto = monto,
            fechaPago = fechaPago,
            comprobanteUri = comprobanteUri,
            validado = false
        )
        return registrarComprobantePago(comprobantePago)
    }

    override suspend fun marcarNotificacionLeida(id: String) {
        val index = notificacionesList.indexOfFirst { it.id == id }
        if (index != -1) {
            notificacionesList[index] = notificacionesList[index].copy(leida = true)
        }
        id.toLongOrNull()?.let { marcarNotificacionLeida(it) }
    }

    override suspend fun getFechasImportantes(): List<FechaImportante> = withContext(ioDispatcher) {
        try {
            refreshFechasImportantes()
        } catch (_: Exception) { }
        val locales = fechaImportanteDao.getFechasVigentesFlow().first().map { entity ->
            FechaImportante(
                id = entity.idFecha.toString(),
                titulo = entity.titulo,
                descripcion = entity.descripcion,
                fechaLimite = entity.fechaLimite,
                diasRestantes = calcularDiasRestantes(entity.fechaLimite),
                tipo = entity.tipo
            )
        }
        locales.ifEmpty { fechasList }
    }

    override suspend fun isReglamentoFirmado(): Boolean = reglamentoFirmado

    override suspend fun firmarReglamento(firmaData: String): Result<Boolean> {
        reglamentoFirmado = true
        return Result.success(true)
    }

    override suspend fun getAspirantesParaRevision(): List<Aspirante> = withContext(ioDispatcher) {
        try {
            val remotos = apiService.getAdminAspirantes().map { mapDtoToAspirante(it) }
            remotos.ifEmpty { aspirantesAdminList.toList() }
        } catch (_: Exception) {
            aspirantesAdminList.toList()
        }
    }

    override suspend fun getAspirantePorId(id: String): Aspirante? = withContext(ioDispatcher) {
        val remoto = id.toLongOrNull()?.let { idLong ->
            try {
                mapDtoToAspirante(apiService.getAdminAspiranteDetalle(idLong))
            } catch (_: Exception) {
                null
            }
        }
        remoto ?: aspirantesAdminList.find { it.id == id } ?: currentAspirante
    }

    override suspend fun revisarDocumento(
        documentoId: String,
        aprobado: Boolean,
        observaciones: String
    ): Result<Boolean> = withContext(ioDispatcher) {
        val nuevoEstado = if (aprobado) EstadoDocumento.VALIDADO else EstadoDocumento.RECHAZADO
        val index = documentosList.indexOfFirst { it.id == documentoId }
        if (index != -1) {
            documentosList[index] = documentosList[index].copy(estado = nuevoEstado, observaciones = observaciones)
        }
        documentoId.toLongOrNull()?.let { idRemoto ->
            try {
                apiService.revisarDocumento(
                    idRemoto,
                    RevisionDto(estado = if (aprobado) "APROBADO" else "RECHAZADO", observaciones = observaciones)
                )
            } catch (_: Exception) { }
        }
        Result.success(true)
    }

    // ==========================================
    // --- MAPEADORES ---
    // ==========================================

    private fun mapEntityToDocumento(entity: DocumentoEntity): Documento? {
        val tipo = TipoDocumento.entries.find { it.name.equals(entity.tipoCodigo, ignoreCase = true) }
            ?: return null
        val estado = when (entity.estado.uppercase()) {
            "VALIDADO", "APROBADO", "ACEPTADO" -> EstadoDocumento.VALIDADO
            "RECHAZADO" -> EstadoDocumento.RECHAZADO
            "PENDIENTE" -> EstadoDocumento.PENDIENTE
            else -> EstadoDocumento.SUBIDO
        }
        return Documento(
            id = (entity.idRemoto ?: entity.idLocal).toString(),
            tipo = tipo,
            nombreArchivo = entity.nombreOriginal,
            uriString = entity.rutaLocal,
            tamanoBytes = entity.tamanoBytes,
            estado = estado,
            observaciones = entity.observaciones ?: "",
            version = entity.version
        )
    }

    private fun mapDtoToAspirante(dto: AspiranteDto): Aspirante {
        val estadoEnum = try {
            EstadoInscripcion.valueOf(dto.estadoInscripcion)
        } catch (_: Exception) {
            EstadoInscripcion.EN_REVISION
        }
        return Aspirante(
            id = dto.idAspirante.toString(),
            nombreCompleto = dto.folio,
            curp = dto.curp,
            email = "",
            telefono = dto.telefono,
            carrera = dto.carreraNombre ?: "Ingeniería en Sistemas Computacionales",
            folioAspirante = dto.folio,
            promedioBachillerato = dto.promedioBachillerato ?: 0.0,
            direccion = dto.direccion ?: "",
            estadoInscripcion = estadoEnum
        )
    }

    private fun calcularDiasRestantes(fecha: String): Int = try {
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(fecha.take(10))).toInt()
    } catch (_: Exception) {
        0
    }

    private fun mapEntityToAspirante(entity: AspiranteEntity, user: UsuarioEntity): Aspirante {
        val estadoEnum = try {
            EstadoInscripcion.valueOf(entity.estadoInscripcion)
        } catch (_: Exception) {
            EstadoInscripcion.EN_REVISION
        }

        return Aspirante(
            id = entity.idAspirante.toString(),
            nombreCompleto = "${user.nombre} ${user.apellidos}".trim(),
            curp = entity.curp,
            email = user.correo,
            telefono = entity.telefono,
            carrera = "Ingeniería en Sistemas Computacionales",
            folioAspirante = entity.folio,
            promedioBachillerato = entity.promedioBachillerato,
            direccion = entity.direccion,
            estadoInscripcion = estadoEnum,
            rol = user.rol
        )
    }
}

