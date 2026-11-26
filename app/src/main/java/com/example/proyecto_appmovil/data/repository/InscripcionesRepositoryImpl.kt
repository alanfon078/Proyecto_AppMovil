package com.example.proyecto_appmovil.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.app.NotificationManagerCompat
import androidx.room.withTransaction
import com.example.proyecto_appmovil.data.local.AppDatabase
import com.example.proyecto_appmovil.data.local.entity.AspiranteEntity
import com.example.proyecto_appmovil.data.local.entity.CarreraEntity
import com.example.proyecto_appmovil.data.local.entity.DocumentoEntity
import com.example.proyecto_appmovil.data.local.entity.UsuarioEntity
import com.example.proyecto_appmovil.data.local.session.SessionManager
import com.example.proyecto_appmovil.data.local.session.SessionTokens
import com.example.proyecto_appmovil.data.remote.api.InscripcionesApiService
import com.example.proyecto_appmovil.data.remote.dto.ActualizarAspiranteRequestDto
import com.example.proyecto_appmovil.data.remote.dto.CambiarPasswordRequestDto
import com.example.proyecto_appmovil.data.remote.dto.CrearFechaImportanteDto
import com.example.proyecto_appmovil.data.remote.dto.LoginRequestDto
import com.example.proyecto_appmovil.data.remote.dto.LoginResponseDto
import com.example.proyecto_appmovil.data.remote.dto.RefreshTokenRequestDto
import com.example.proyecto_appmovil.data.remote.dto.RegisterRequestDto
import com.example.proyecto_appmovil.data.remote.dto.RevisionDto
import com.example.proyecto_appmovil.data.remote.dto.CrearPreguntaDto
import com.example.proyecto_appmovil.data.remote.dto.ResponderPreguntaDto
import com.example.proyecto_appmovil.data.remote.dto.AvisoGeneralDto
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.Notificacion
import com.example.proyecto_appmovil.model.ResumenAspirante
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.model.PreguntaDuda
import com.example.proyecto_appmovil.util.FileHashHelper
import com.example.proyecto_appmovil.util.PdfMetadata
import com.example.proyecto_appmovil.work.NotificacionesWorkManager
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.Locale

class InscripcionesRepositoryImpl(
    private val context: Context,
    private val database: AppDatabase,
    private val api: InscripcionesApiService,
    private val session: SessionManager
) : InscripcionesRepository {
    private val usuarios = database.usuarioDao()
    private val aspirantes = database.aspiranteDao()
    private val documentos = database.documentoDao()
    private val notificaciones = database.notificacionDao()
    private val fechas = database.fechaImportanteDao()
    private val catalogos = database.catalogosDao()
    private val preguntas = database.preguntaDudaDao()

    // Serializa las operaciones de caché con el cierre de sesión.
    private val operationMutex = Mutex()

    override suspend fun login(usuarioOCorreo: String, contrasena: String): Result<Aspirante> = request {
        installSession(api.login(LoginRequestDto(usuarioOCorreo.trim(), contrasena)))
    }

    private suspend fun installSession(response: LoginResponseDto): Aspirante {
        val user = response.usuario
        val usuario = UsuarioEntity(
            user.idUsuario, user.nombre, user.apellidos, user.nombreUsuario, user.correo, user.rol, user.activo
        )
        val aspirante = response.aspirante?.toEntity()
        val model = aspirante?.toModel(usuario) ?: Aspirante(
            id = user.idUsuario.toString(),
            nombreCompleto = "${user.nombre} ${user.apellidos}".trim(),
            email = user.correo,
            rol = user.rol
        )
        val tokens = SessionTokens(response.accessToken, response.refreshToken)

        clearLocalSession()
        database.withTransaction {
            usuarios.insertOrUpdate(usuario)
            aspirante?.let { aspirantes.insertOrUpdate(it) }
        }
        session.saveSession(tokens, user.idUsuario, aspirante?.idAspirante, user.rol, user.correo)
        return model
    }

    override suspend fun registrarAspirante(
        nombreCompleto: String,
        nombreUsuario: String,
        curp: String,
        email: String,
        telefono: String,
        idCarrera: Int,
        direccion: String,
        estadoResidencia: String,
        municipioResidencia: String,
        contrasena: String
    ): Result<Boolean> = request {
        requirePasswordLength(contrasena)
        val response = api.register(RegisterRequestDto(
            nombreCompleto.trim(), nombreUsuario.trim().lowercase(Locale.ROOT),
            curp.trim().uppercase(Locale.ROOT), email.trim(), telefono.trim(),
            idCarrera, contrasena, direccion.trim(), estadoResidencia.trim(), municipioResidencia.trim()
        ))
        installSession(response)
        true
    }

    override suspend fun verificarDisponibilidadUsuario(nombreUsuario: String): Result<Boolean> = request {
        api.verificarDisponibilidadUsuario(nombreUsuario.trim().lowercase(Locale.ROOT)).disponible
    }

    override suspend fun cambiarPassword(actual: String, nueva: String): Result<Unit> = request {
        requirePasswordLength(nueva)
        api.cambiarPassword(CambiarPasswordRequestDto(actual, nueva)).requireSuccess()
    }

    override suspend fun logout(): Result<Unit> = request {
        try {
            session.getTokens()?.let { api.logout(RefreshTokenRequestDto(it.refreshToken)).requireSuccess() }
        } finally {
            withContext(NonCancellable) { clearLocalSession() }
        }
    }

    private fun requirePasswordLength(password: String) {
        require(Character.codePointCount(password, 0, password.length) >= 8) { "La contraseña debe tener mínimo 8 caracteres." }
    }

    private fun clearLocalSession() {
        session.clearSession()
        NotificacionesWorkManager.cancel(context)
        NotificationManagerCompat.from(context).cancelAll()
        database.clearAllTables()
        FileHashHelper.docsDirectory(context).deleteRecursively()
        File(context.filesDir, "documentos").deleteRecursively()
        File(context.cacheDir, "preview_docs").deleteRecursively()
    }

    override suspend fun getAspiranteActual(): Aspirante = request {
        val aspirante = api.getMiAspirante()
        aspirantes.insertOrUpdate(aspirante.toEntity())
        aspirante.toModel()
    }.getOrThrow()

    override suspend fun getMiResumen(): ResumenAspirante = request {
        val resumen = api.getMiResumen()
        aspirantes.insertOrUpdate(resumen.aspirante.toEntity())
        val docs = resumen.documentos
        ResumenAspirante(
            resumen.aspirante.toModel(), docs.requeridos, docs.subidos, docs.aprobados, docs.pendientes,
            docs.rechazados, resumen.notificacionesNoLeidas
        )
    }.getOrThrow()

    override suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean> = request {
        val updated = api.actualizarMiAspirante(ActualizarAspiranteRequestDto(
            aspirante.telefono.trim(),
            aspirante.direccion.trim(),
            aspirante.estadoResidencia.trim(),
            aspirante.municipioResidencia.trim()
        ))
        aspirantes.insertOrUpdate(updated.toEntity())
        true
    }

    override suspend fun getDocumentos(): List<Documento> = request {
        val response = api.getDocumentos().map { it.toEntity() }
        database.withTransaction {
            documentos.clearAll()
            documentos.insertAll(response)
        }
        response.completeSlots()
    }.getOrThrow()

    override suspend fun obtenerMetadataPdf(uri: Uri): Result<PdfMetadata> = request {
        FileHashHelper.inspectPdf(context, uri)
    }

    override suspend fun subirDocumento(tipo: TipoDocumento, uri: Uri): Result<Boolean> = request {
        val pdf = FileHashHelper.copyToInternalAndComputeSha256(context, uri)
        try {
            val filePart = MultipartBody.Part.createFormData(
                "archivo", pdf.originalName, pdf.file.asRequestBody(FileHashHelper.PDF_MIME_TYPE.toMediaType())
            )
            val plainText = "text/plain".toMediaType()
            val response = api.subirDocumento(
                filePart, tipo.name.toRequestBody(plainText), pdf.sha256Hex.toRequestBody(plainText)
            )
            val documento = response.documento.toEntity()
            database.withTransaction {
                documentos.deleteByType(documento.idAspirante, tipo.name)
                documentos.insertAll(listOf(documento))
                aspirantes.actualizarEstado(documento.idAspirante, response.estadoInscripcion)
            }
            true
        } finally {
            pdf.file.delete()
        }
    }

    override suspend fun eliminarDocumento(id: String): Result<Unit> = request {
        val remoteId = parseId(id)
        api.eliminarDocumento(remoteId).requireSuccess()
        documentos.deleteByRemoteId(remoteId)
    }

    override suspend fun getNotificaciones(): List<Notificacion> = request {
        val response = api.getNotificaciones().map { it.toEntity() }
        database.withTransaction {
            notificaciones.clearAll()
            notificaciones.insertAll(response)
        }
        response.map { it.toModel() }
    }.getOrThrow()

    override fun observarNotificaciones(): Flow<List<Notificacion>> =
        notificaciones.observarTodas().map { rows -> rows.map { it.toModel() } }

    override suspend fun marcarNotificacionLeida(id: String) = request {
        val remoteId = parseId(id)
        api.marcarNotificacionLeida(remoteId).requireSuccess()
        notificaciones.marcarLeida(remoteId)
    }.getOrThrow()

    override suspend fun marcarTodasNotificacionesLeidas(): Result<Unit> = request {
        api.marcarTodasNotificacionesLeidas().requireSuccess()
        notificaciones.marcarTodasLeidas()
    }

    override suspend fun eliminarNotificacion(id: Long): Result<Boolean> = request {
        api.eliminarNotificacion(id).requireSuccess()
        notificaciones.eliminar(id)
        true
    }

    override suspend fun getFechasImportantes(): List<FechaImportante> = request {
        val response = api.getFechasImportantes()
        database.withTransaction {
            fechas.clearAll()
            fechas.insertAll(response.map { it.toEntity() })
        }
        response.map {
            FechaImportante(
                it.idFecha.toString(), it.titulo, it.descripcion.orEmpty(), it.fechaLimite, it.diasRestantes, it.tipo
            )
        }
    }.getOrThrow()

    override fun observarFechasImportantes(): Flow<List<FechaImportante>> =
        fechas.getFechasVigentesFlow().map { rows -> rows.map { it.toModel() } }

    override suspend fun crearFechaImportante(
        titulo: String,
        descripcion: String,
        tipo: String,
        fechaLimite: String
    ): Result<Unit> = request {
        val creada = api.crearFechaImportante(CrearFechaImportanteDto(titulo, descripcion, tipo, fechaLimite))
        fechas.insertAll(listOf(creada.toEntity()))
    }

    override suspend fun actualizarFechaImportante(
        id: String,
        titulo: String,
        descripcion: String,
        tipo: String,
        fechaLimite: String
    ): Result<Unit> = request {
        val actualizada = api.actualizarFechaImportante(
            parseId(id),
            CrearFechaImportanteDto(titulo, descripcion, tipo, fechaLimite)
        )
        fechas.insertAll(listOf(actualizada.toEntity()))
    }

    override suspend fun eliminarFechaImportante(id: String): Result<Unit> = request {
        val remoteId = parseId(id)
        api.eliminarFechaImportante(remoteId).requireSuccess()
        fechas.eliminar(remoteId)
    }

    override fun getCarrerasFlow(): Flow<List<CarreraEntity>> = catalogos.getCarrerasFlow()

    override suspend fun refreshCatalogos(): Result<Boolean> = request {
        val response = api.getCarreras()
        database.withTransaction {
            catalogos.clearCarreras()
            catalogos.insertCarreras(response.map { CarreraEntity(it.idCarrera, it.nombre, it.activa) })
        }
        true
    }

    override suspend fun getAspirantesParaRevision(estado: String?, q: String?): List<Aspirante> = request {
        api.getAdminAspirantes(estado, q).map { it.toModel() }
    }.getOrThrow()

    override suspend fun getAspirantePorId(id: String): Aspirante? = request {
        api.getAdminAspiranteDetalle(parseId(id)).toModel()
    }.getOrThrow()

    override suspend fun getDocumentosAspirante(id: String): List<Documento> = request {
        api.getDocumentosAspirante(parseId(id)).filter { it.id != null }.map { it.toEntity() }.completeSlots()
    }.getOrThrow()

    override suspend fun descargarDocumentoParaPrevisualizacion(
        documentoId: String,
        nombreArchivo: String?
    ): Result<File> = request {
        val remoteId = parseId(documentoId)
        val response = api.descargarArchivoDocumento(remoteId)
        if (!response.isSuccessful) throw HttpException(response)
        val body = response.body() ?: throw IllegalStateException("El servidor devolvió un archivo vacío.")

        body.use { responseBody ->
            val previewDir = File(context.cacheDir, "preview_docs").apply {
                if (!isDirectory && !mkdirs()) {
                    throw IllegalStateException("No se pudo preparar la carpeta temporal para previsualizar el PDF.")
                }
            }
            val safeName = nombreArchivo
                ?.replace(Regex("[^A-Za-z0-9._-]"), "_")
                ?.takeIf { it.isNotBlank() }
                ?.let { if (it.endsWith(".pdf", ignoreCase = true)) it else "$it.pdf" }
                ?: "documento_$remoteId.pdf"
            val targetFile = File(previewDir, "${remoteId}_$safeName")
            val tempFile = File.createTempFile("preview_", ".tmp", previewDir)
            try {
                val downloadContext = currentCoroutineContext()
                val input = responseBody.byteStream()
                tempFile.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        downloadContext.ensureActive()
                        val bytesRead = input.read(buffer)
                        if (bytesRead == -1) break
                        output.write(buffer, 0, bytesRead)
                    }
                }
                downloadContext.ensureActive()
                if (tempFile.length() < 5L) {
                    throw IllegalStateException("El archivo PDF descargado no es válido o está vacío.")
                }
                val header = ByteArray(5)
                tempFile.inputStream().use { it.read(header) }
                if (!header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))) {
                    throw IllegalStateException("El archivo descargado no tiene un formato PDF válido.")
                }
                check(tempFile.renameTo(targetFile)) { "No se pudo guardar la previsualización del PDF." }
                targetFile
            } finally {
                if (tempFile.exists()) tempFile.delete()
            }
        }
    }

    override suspend fun revisarDocumento(
        documentoId: String, aprobado: Boolean, observaciones: String
    ): Result<Boolean> = request {
        api.revisarDocumento(
            parseId(documentoId),
            RevisionDto(if (aprobado) "aprobado" else "rechazado", observaciones.trim())
        )
        true
    }

    override suspend fun aprobarAspirante(id: String): Result<Aspirante> = request {
        api.aprobarAspirante(parseId(id)).toModel()
    }

    override suspend fun crearPregunta(categoria: String, pregunta: String): Result<PreguntaDuda> = request {
        val response = api.crearPregunta(CrearPreguntaDto(categoria, pregunta))
        database.withTransaction { preguntas.insertAll(listOf(response.toEntity())) }
        response.toModel()
    }

    override suspend fun getMisPreguntas(): List<PreguntaDuda> = request {
        val response = api.getMisPreguntas()
        database.withTransaction { preguntas.insertAll(response.map { it.toEntity() }) }
        response.map { it.toModel() }
    }.getOrThrow()

    override suspend fun getPreguntasAdmin(categoria: String?, estado: String?): List<PreguntaDuda> = request {
        val response = api.getPreguntasAdmin(categoria, estado)
        database.withTransaction { preguntas.insertAll(response.map { it.toEntity() }) }
        response.map { it.toModel() }
    }.getOrThrow()

    override suspend fun responderPregunta(id: String, respuesta: String): Result<PreguntaDuda> = request {
        val response = api.responderPregunta(parseId(id), ResponderPreguntaDto(respuesta))
        database.withTransaction { preguntas.insertAll(listOf(response.toEntity())) }
        response.toModel()
    }

    override suspend fun enviarAvisoGeneral(titulo: String, mensaje: String): Result<Int> = request {
        api.enviarAvisoGeneral(AvisoGeneralDto(titulo, mensaje)).notificados
    }

    override suspend fun sincronizarNotificaciones(publicar: (Notificacion) -> Boolean): Unit = request {
        if (!session.isLoggedIn() || session.getUserRole() != "aspirante") return@request
        val response = api.getNotificacionesNuevas(false, session.getUltimoIdNotificado())
            .map { it.toEntity() }
        notificaciones.insertAll(response)
        for (aviso in response) {
            if (!publicar(aviso.toModel())) break
            session.saveUltimoIdNotificado(aviso.idNotificacion)
        }
    }.getOrThrow()

    private fun parseId(id: String): Long =
        id.toLongOrNull()?.takeIf { it > 0 } ?: throw IllegalArgumentException("Identificador inválido.")

    private suspend fun <T> request(action: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        operationMutex.withLock {
            try {
                Result.success(action())
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: HttpException) {
                Result.failure(IllegalStateException(httpMessage(exception), exception))
            } catch (exception: ConnectException) {
                Result.failure(IllegalStateException("No se pudo conectar al servidor.", exception))
            } catch (exception: UnknownHostException) {
                Result.failure(IllegalStateException("No se encontró el servidor.", exception))
            } catch (exception: SocketTimeoutException) {
                Result.failure(IllegalStateException("El servidor tardó demasiado en responder.", exception))
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        }
    }

    private fun Response<*>.requireSuccess() {
        if (!isSuccessful) throw HttpException(this)
    }

    private fun httpMessage(exception: HttpException): String {
        val message = exception.response()?.errorBody()?.use { body ->
            try {
                val error = JsonParser.parseReader(body.charStream()).takeIf { it.isJsonObject }?.asJsonObject
                error?.get("error")
                    ?.takeIf { it.isJsonObject }
                    ?.asJsonObject?.get("message")
                    ?.takeIf { it.isJsonPrimitive }
                    ?.asString
            } catch (_: JsonParseException) {
                null
            }
        }
        return message ?: "El servidor respondió con un error (${exception.code()})."
    }
}
