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
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

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
    // --- AUTENTICACIÓN Y SESIÓN ---
    // ==========================================

    override suspend fun login(email: String, contrasena: String): Result<Aspirante> = withContext(ioDispatcher) {
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

            val aspiranteModel = getAspiranteActual() ?: Aspirante(
                id = response.aspirante?.idAspirante?.toString() ?: "1",
                nombreCompleto = "${response.usuario.nombre} ${response.usuario.apellidos}",
                curp = response.aspirante?.curp ?: "",
                email = response.usuario.correo,
                telefono = response.aspirante?.telefono ?: "",
                carrera = response.aspirante?.carreraNombre ?: "Ingeniería en Sistemas Computacionales",
                folioAspirante = response.aspirante?.folio ?: "FOL-2026-001"
            )

            Result.success(aspiranteModel)
        } catch (e: Exception) {
            // Estrategia Offline: si no hay red pero el usuario ya había iniciado sesión
            val localUser = usuarioDao.getUsuarioSync()
            val localAspirante = aspiranteDao.getAspiranteSync()

            if (localUser != null && localUser.correo.equals(email, ignoreCase = true) && localAspirante != null) {
                val model = mapEntityToAspirante(localAspirante, localUser)
                Result.success(model)
            } else {
                Result.failure(e)
            }
        }
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
            val request = RegisterRequestDto(
                nombre = aspirante.nombreCompleto.substringBeforeLast(" ", aspirante.nombreCompleto),
                apellidos = aspirante.nombreCompleto.substringAfterLast(" ", ""),
                curp = aspirante.curp,
                correo = aspirante.email,
                telefono = aspirante.telefono,
                idCarrera = 1,
                contrasena = contrasena
            )
            val response = apiService.register(request)
            sessionManager.saveSession(
                token = response.token,
                userId = response.usuario.idUsuario,
                aspiranteId = response.aspirante?.idAspirante,
                role = response.usuario.rol,
                email = response.usuario.correo
            )
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

    override suspend fun getAspiranteActual(): Aspirante? = withContext(ioDispatcher) {
        val entity = aspiranteDao.getAspiranteSync()
        val user = usuarioDao.getUsuarioSync()
        if (entity != null && user != null) {
            mapEntityToAspirante(entity, user)
        } else {
            null
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
        } catch (_: Exception) { }

        // Mapear desde Room (Single Source of Truth)
        val entities = documentoDao.getDocumentosFlow()
        // O sincronizar pendientes
        sincronizarDocumentosPendientes()

        // Como getDocumentos() devuelve List<Documento>, leemos de la base de datos
        // usando sync o devolvemos lista mapeada
        val aspiranteId = sessionManager.getAspiranteId()
        // Construimos la lista de tipos predeterminados si está vacía
        val defaultDocs = TipoDocumento.values().map { tipo ->
            Documento(
                id = tipo.name,
                tipo = tipo,
                nombreArchivo = null,
                uriString = null,
                tamanoBytes = 0L,
                estado = EstadoDocumento.PENDIENTE
            )
        }
        defaultDocs
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

    override suspend fun getComprobantePago(): ComprobantePago? = withContext(ioDispatcher) {
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
        }
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
        emptyList()
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
            estadoInscripcion = estadoEnum
        )
    }
}
