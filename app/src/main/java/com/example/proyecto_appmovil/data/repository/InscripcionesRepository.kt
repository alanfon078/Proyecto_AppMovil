package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.ComprobantePago
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.Notificacion

interface InscripcionesRepository {
    // Autenticación y Perfil
    suspend fun login(email: String, contrasena: String): Result<Aspirante>
    suspend fun registrarAspirante(aspirante: Aspirante, contrasena: String): Result<Boolean>
    suspend fun getAspiranteActual(): Aspirante?
    suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean>

    // Documentos
    suspend fun getDocumentos(): List<Documento>
    suspend fun subirDocumento(documento: Documento): Result<Boolean>

    // Notificaciones
    suspend fun getNotificaciones(): List<Notificacion>

    // Comprobante de pago
    suspend fun getComprobantePago(): ComprobantePago?
    suspend fun registrarComprobantePago(comprobante: ComprobantePago): Result<Boolean>
}
