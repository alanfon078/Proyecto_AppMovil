package com.example.proyecto_appmovil.data.remote.api

import com.example.proyecto_appmovil.data.remote.dto.*

// TODO: Agregar anotaciones de Retrofit (@POST, @GET, @Multipart) al integrar Retrofit
interface InscripcionesApiService {
    suspend fun login(request: LoginRequestDto): LoginResponseDto
    suspend fun register(request: RegisterRequestDto): LoginResponseDto
    suspend fun getDocumentos(aspiranteId: String): List<DocumentoDto>
    suspend fun getNotificaciones(aspiranteId: String): List<NotificacionDto>
}
