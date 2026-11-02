package com.example.proyecto_appmovil.data.repository

import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.ComprobantePago
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.Notificacion

class InscripcionesRepositoryImpl : InscripcionesRepository {

    override suspend fun login(email: String, contrasena: String): Result<Aspirante> {
        // TODO: Orquestar llamada remota con Retrofit y guardado en Room
        return Result.failure(NotImplementedError("Por implementar"))
    }

    override suspend fun registrarAspirante(aspirante: Aspirante, contrasena: String): Result<Boolean> {
        // TODO: Orquestar llamada remota con Retrofit y guardado en Room
        return Result.failure(NotImplementedError("Por implementar"))
    }

    override suspend fun getAspiranteActual(): Aspirante? {
        // TODO: Consultar aspirante desde Room / cache
        return null
    }

    override suspend fun actualizarAspirante(aspirante: Aspirante): Result<Boolean> {
        // TODO: Actualizar aspirante en Room y backend
        return Result.failure(NotImplementedError("Por implementar"))
    }

    override suspend fun getDocumentos(): List<Documento> {
        // TODO: Consultar documentos desde Room o Remote
        return emptyList()
    }

    override suspend fun subirDocumento(documento: Documento): Result<Boolean> {
        // TODO: Subir documento a la API y actualizar Room
        return Result.failure(NotImplementedError("Por implementar"))
    }

    override suspend fun getNotificaciones(): List<Notificacion> {
        // TODO: Consultar notificaciones desde Room o Remote
        return emptyList()
    }

    override suspend fun getComprobantePago(): ComprobantePago? {
        // TODO: Consultar comprobante de pago
        return null
    }

    override suspend fun registrarComprobantePago(comprobante: ComprobantePago): Result<Boolean> {
        // TODO: Registrar comprobante de pago en API y Room
        return Result.failure(NotImplementedError("Por implementar"))
    }
}
