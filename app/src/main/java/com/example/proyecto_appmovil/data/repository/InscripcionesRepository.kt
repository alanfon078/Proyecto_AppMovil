package com.example.proyecto_appmovil.data.repository

import android.net.Uri
import com.example.proyecto_appmovil.data.local.entity.CarreraEntity
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.Notificacion
import com.example.proyecto_appmovil.model.ResumenAspirante
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.model.PreguntaDuda
import com.example.proyecto_appmovil.util.PdfMetadata
import java.io.File
import kotlinx.coroutines.flow.Flow

interface InscripcionesRepository {
    suspend fun login(usuarioOCorreo: String, contrasena: String): Result<Aspirante>
    suspend fun registrarAspirante(
        nombreCompleto: String,
        nombreUsuario: String,
        curp: String,
        email: String,
        telefono: String,
        idCarrera: Int,
        direccion: String = "",
        estadoResidencia: String,
        municipioResidencia: String,
        contrasena: String
    ): Result<Boolean>
    suspend fun verificarDisponibilidadUsuario(nombreUsuario: String): Result<Boolean>
    suspend fun logout(): Result<Unit>
    suspend fun cambiarPassword(actual: String, nueva: String): Result<Unit>

    suspend fun getAspiranteActual(): Aspirante
    suspend fun getMiResumen(): ResumenAspirante
    suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean>

    suspend fun getDocumentos(): List<Documento>
    suspend fun obtenerMetadataPdf(uri: Uri): Result<PdfMetadata>
    suspend fun subirDocumento(tipo: TipoDocumento, uri: Uri): Result<Boolean>
    suspend fun eliminarDocumento(id: String): Result<Unit>

    suspend fun getNotificaciones(): List<Notificacion>
    fun observarNotificaciones(): Flow<List<Notificacion>>
    suspend fun marcarNotificacionLeida(id: String)
    suspend fun marcarTodasNotificacionesLeidas(): Result<Unit>
    suspend fun eliminarNotificacion(id: Long): Result<Boolean>

    suspend fun getFechasImportantes(): List<FechaImportante>
    fun observarFechasImportantes(): Flow<List<FechaImportante>>
    suspend fun crearFechaImportante(
        titulo: String,
        descripcion: String,
        tipo: String,
        fechaLimite: String
    ): Result<Unit>
    suspend fun actualizarFechaImportante(
        id: String,
        titulo: String,
        descripcion: String,
        tipo: String,
        fechaLimite: String
    ): Result<Unit>
    suspend fun eliminarFechaImportante(id: String): Result<Unit>

    fun getCarrerasFlow(): Flow<List<CarreraEntity>>
    suspend fun refreshCatalogos(): Result<Boolean>

    suspend fun getAspirantesParaRevision(estado: String? = null, q: String? = null): List<Aspirante>
    suspend fun getAspirantePorId(id: String): Aspirante?
    suspend fun getDocumentosAspirante(id: String): List<Documento>
    suspend fun descargarDocumentoParaPrevisualizacion(documentoId: String, nombreArchivo: String?): Result<File>
    suspend fun revisarDocumento(documentoId: String, aprobado: Boolean, observaciones: String): Result<Boolean>
    suspend fun aprobarAspirante(id: String): Result<Aspirante>

    suspend fun crearPregunta(categoria: String, pregunta: String): Result<PreguntaDuda>
    suspend fun getMisPreguntas(): List<PreguntaDuda>
    suspend fun getPreguntasAdmin(categoria: String? = null, estado: String? = null): List<PreguntaDuda>
    suspend fun responderPregunta(id: String, respuesta: String): Result<PreguntaDuda>
    suspend fun enviarAvisoGeneral(titulo: String, mensaje: String): Result<Int>
    suspend fun sincronizarNotificaciones(publicar: (Notificacion) -> Boolean)
}
