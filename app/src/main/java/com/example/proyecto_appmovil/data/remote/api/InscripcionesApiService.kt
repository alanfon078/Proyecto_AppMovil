package com.example.proyecto_appmovil.data.remote.api

import com.example.proyecto_appmovil.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface InscripcionesApiService {

    // --- Autenticación ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): LoginResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body body: Map<String, String>): LoginResponseDto

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @GET("auth/me")
    suspend fun getMe(): UserDto

    // --- Catálogos ---
    @GET("catalogos/carreras")
    suspend fun getCarreras(): List<CarreraDto>

    @GET("catalogos/estados")
    suspend fun getEstados(): List<EstadoDto>

    @GET("catalogos/municipios")
    suspend fun getMunicipios(@Query("idEstado") idEstado: Int? = null): List<MunicipioDto>

    // --- Aspirante (Perfil & Resumen) ---
    @GET("aspirantes/me")
    suspend fun getMiAspirante(): AspiranteDto

    @GET("aspirantes/me/resumen")
    suspend fun getMiResumen(): AspiranteDto

    @GET("aspirantes/me/estatus")
    suspend fun getMiEstatus(): Map<String, String>

    @PUT("aspirantes/me")
    suspend fun actualizarMiAspirante(@Body body: Map<String, @JvmSuppressWildcards Any?>): AspiranteDto

    // --- Documentos (Multimedia) ---
    @GET("aspirantes/me/documentos")
    suspend fun getDocumentos(): List<DocumentoItemDto>

    @Multipart
    @POST("aspirantes/me/documentos")
    suspend fun subirDocumento(
        @Part archivo: MultipartBody.Part,
        @Part("idTipoDocumento") idTipoDocumento: RequestBody,
        @Part("tipoCodigo") tipoCodigo: RequestBody
    ): SubirDocumentoResponseDto

    @DELETE("documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Long): Response<Unit>

    // --- Notificaciones ---
    @GET("notificaciones")
    suspend fun getNotificaciones(): List<NotificacionItemDto>

    @PATCH("notificaciones/{id}/leida")
    suspend fun marcarNotificacionLeida(
        @Path("id") id: Long,
        @Body request: MarcarLeidaRequestDto = MarcarLeidaRequestDto(true)
    ): Response<Unit>

    @DELETE("notificaciones/{id}")
    suspend fun eliminarNotificacion(@Path("id") id: Long): Response<Unit>

    // --- Fechas Importantes ---
    @GET("fechas-importantes")
    suspend fun getFechasImportantes(): List<FechaImportanteDto>

    // --- Comprobante de Pago ---
    @GET("aspirantes/me/pago")
    suspend fun getPago(): PagoResponseDto?

    @Multipart
    @POST("aspirantes/me/pago")
    suspend fun subirPago(
        @Part archivo: MultipartBody.Part,
        @Part("folioPago") folioPago: RequestBody,
        @Part("monto") monto: RequestBody,
        @Part("fechaPago") fechaPago: RequestBody
    ): PagoResponseDto

    // --- Panel Administrativo ---
    @GET("admin/aspirantes")
    suspend fun getAdminAspirantes(): List<AspiranteDto>

    @GET("admin/aspirantes/{id}")
    suspend fun getAdminAspiranteDetalle(@Path("id") id: Long): AspiranteDto

    @POST("admin/documentos/{id}/revision")
    suspend fun revisarDocumento(
        @Path("id") id: Long,
        @Body revision: RevisionDto
    ): DocumentoItemDto
}
