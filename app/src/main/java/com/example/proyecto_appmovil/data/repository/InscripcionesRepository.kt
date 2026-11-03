package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.model.*

interface InscripcionesRepository {
    // Autenticación y Perfil
    suspend fun login(usuarioOCorreo: String, contrasena: String): Result<Aspirante>
    suspend fun registrarAspirante(
        nombreCompleto: String,
        curp: String,
        email: String,
        telefono: String,
        carrera: String,
        direccion: String,
        contrasena: String
    ): Result<Boolean>
    suspend fun getAspiranteActual(): Aspirante
    suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean>

    // Documentos
    suspend fun getDocumentos(): List<Documento>
    suspend fun subirDocumento(tipo: TipoDocumento, nombreArchivo: String, uriString: String, tamanoBytes: Long): Result<Boolean>

    // Notificaciones y Fechas
    suspend fun getNotificaciones(): List<Notificacion>
    suspend fun marcarNotificacionLeida(id: String)
    suspend fun getFechasImportantes(): List<FechaImportante>

    // Comprobante de pago
    suspend fun getComprobantePago(): ComprobantePago
    suspend fun registrarComprobantePago(folio: String, monto: Double, fechaPago: String, comprobanteUri: String): Result<Boolean>

    // Reglamento institucional
    suspend fun isReglamentoFirmado(): Boolean
    suspend fun firmarReglamento(firmaData: String): Result<Boolean>

    // Control Escolar (Admin)
    suspend fun getAspirantesParaRevision(): List<Aspirante>
    suspend fun getAspirantePorId(id: String): Aspirante?
    suspend fun revisarDocumento(documentoId: String, aprobado: Boolean, observaciones: String): Result<Boolean>
}
