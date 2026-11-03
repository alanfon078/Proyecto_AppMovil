package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.data.local.entity.*
import com.example.proyecto_appmovil.data.remote.dto.*
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.ComprobantePago
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.Notificacion
import com.example.proyecto_appmovil.model.TipoDocumento
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Contrato único del repositorio de inscripciones.
 *
 * Combina:
 *  - La capa de datos (Room + Retrofit + Sesión) de feature/capa-datos-room-retrofit-di.
 *  - Las operaciones de alto nivel que consumen las pantallas Compose de
 *    feature/ui-compose-navegacion-navhost (modelos de dominio listos para la UI).
 */
interface InscripcionesRepository {

    // --- Autenticación y Sesión ---
    suspend fun login(usuarioOCorreo: String, contrasena: String): Result<Aspirante>
    suspend fun loginApi(email: String, contrasena: String): Result<LoginResponseDto>
    suspend fun registrarAspirante(aspirante: Aspirante, contrasena: String): Result<Boolean>
    suspend fun registrarAspirante(
        nombreCompleto: String,
        curp: String,
        email: String,
        telefono: String,
        carrera: String,
        direccion: String,
        contrasena: String
    ): Result<Boolean>
    suspend fun registerApi(request: RegisterRequestDto): Result<LoginResponseDto>
    suspend fun logout(): Result<Unit>
    fun isLoggedIn(): Boolean
    fun getUsuarioActualFlow(): Flow<UsuarioEntity?>
    suspend fun getUsuarioActualSync(): UsuarioEntity?

    // --- Perfil de Aspirante ---
    fun getAspiranteFlow(): Flow<AspiranteEntity?>
    suspend fun getAspiranteActual(): Aspirante
    suspend fun getAspiranteSync(): AspiranteEntity?
    suspend fun refreshAspirante(): Result<AspiranteEntity>
    suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean>
    suspend fun actualizarDatosPersonales(
        telefono: String,
        direccion: String,
        idEstado: Int?,
        idMunicipio: Int?
    ): Result<Boolean>
    suspend fun getEstatusInscripcion(): Result<Map<String, String>>

    // --- Documentos (Multimedia) ---
    fun getDocumentosFlow(): Flow<List<DocumentoEntity>>
    fun getDocumentoPorTipoFlow(tipoCodigo: String): Flow<DocumentoEntity?>
    suspend fun getDocumentos(): List<Documento>
    suspend fun refreshDocumentos(): Result<List<DocumentoEntity>>
    suspend fun subirDocumento(documento: Documento): Result<Boolean>
    suspend fun subirDocumento(tipo: TipoDocumento, nombreArchivo: String, uriString: String, tamanoBytes: Long): Result<Boolean>
    suspend fun subirDocumentoArchivo(
        tipoCodigo: String,
        idTipoDocumento: Int,
        archivo: File,
        nombreOriginal: String,
        mimeType: String
    ): Result<DocumentoEntity>
    suspend fun eliminarDocumento(idLocal: Long, idRemoto: Long?): Result<Boolean>
    suspend fun sincronizarDocumentosPendientes(): Result<Int>

    // --- Comprobante de Pago (Multimedia) ---
    fun getPagoFlow(): Flow<ComprobantePagoEntity?>
    suspend fun getComprobantePago(): ComprobantePago
    suspend fun getPagoSync(): ComprobantePagoEntity?
    suspend fun refreshPago(): Result<ComprobantePagoEntity?>
    suspend fun registrarComprobantePago(comprobante: ComprobantePago): Result<Boolean>
    suspend fun registrarComprobantePago(folio: String, monto: Double, fechaPago: String, comprobanteUri: String): Result<Boolean>
    suspend fun subirComprobantePago(
        folioPago: String,
        monto: Double,
        fechaPago: String,
        archivo: File?,
        rutaLocal: String?
    ): Result<ComprobantePagoEntity>

    // --- Notificaciones ---
    fun getNotificacionesFlow(soloNoLeidas: Boolean = false): Flow<List<NotificacionEntity>>
    fun countNotificacionesNoLeidasFlow(): Flow<Int>
    suspend fun getNotificaciones(): List<Notificacion>
    suspend fun refreshNotificaciones(): Result<List<NotificacionEntity>>
    suspend fun marcarNotificacionLeida(id: Long): Result<Boolean>
    suspend fun marcarNotificacionLeida(id: String)
    suspend fun eliminarNotificacion(id: Long): Result<Boolean>

    // --- Fechas Importantes ---
    fun getFechasImportantesFlow(): Flow<List<FechaImportanteEntity>>
    suspend fun refreshFechasImportantes(): Result<List<FechaImportanteEntity>>
    suspend fun getFechasImportantes(): List<FechaImportante>

    // --- Catálogos (Offline-first Cache) ---
    fun getCarrerasFlow(): Flow<List<CarreraEntity>>
    fun getEstadosFlow(): Flow<List<EstadoEntity>>
    fun getMunicipiosFlow(idEstado: Int): Flow<List<MunicipioEntity>>
    suspend fun refreshCatalogos(): Result<Boolean>

    // --- Reglamento institucional ---
    suspend fun isReglamentoFirmado(): Boolean
    suspend fun firmarReglamento(firmaData: String): Result<Boolean>

    // --- Control Escolar (Admin) ---
    suspend fun getAspirantesParaRevision(): List<Aspirante>
    suspend fun getAspirantePorId(id: String): Aspirante?
    suspend fun revisarDocumento(documentoId: String, aprobado: Boolean, observaciones: String): Result<Boolean>
}
