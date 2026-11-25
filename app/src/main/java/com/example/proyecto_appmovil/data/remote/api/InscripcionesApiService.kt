package com.example.proyecto_appmovil.data.remote.api

import com.example.proyecto_appmovil.data.remote.dto.ActualizarAspiranteRequestDto
import com.example.proyecto_appmovil.data.remote.dto.AspiranteDto
import com.example.proyecto_appmovil.data.remote.dto.CambiarPasswordRequestDto
import com.example.proyecto_appmovil.data.remote.dto.CarreraDto
import com.example.proyecto_appmovil.data.remote.dto.CrearFechaImportanteDto
import com.example.proyecto_appmovil.data.remote.dto.DocumentoItemDto
import com.example.proyecto_appmovil.data.remote.dto.FechaImportanteDto
import com.example.proyecto_appmovil.data.remote.dto.LoginRequestDto
import com.example.proyecto_appmovil.data.remote.dto.LoginResponseDto
import com.example.proyecto_appmovil.data.remote.dto.NotificacionItemDto
import com.example.proyecto_appmovil.data.remote.dto.RefreshTokenRequestDto
import com.example.proyecto_appmovil.data.remote.dto.RefreshTokenResponseDto
import com.example.proyecto_appmovil.data.remote.dto.RegisterRequestDto
import com.example.proyecto_appmovil.data.remote.dto.CrearPreguntaDto
import com.example.proyecto_appmovil.data.remote.dto.ResponderPreguntaDto
import com.example.proyecto_appmovil.data.remote.dto.PreguntaDto
import com.example.proyecto_appmovil.data.remote.dto.AvisoGeneralDto
import com.example.proyecto_appmovil.data.remote.dto.AvisoGeneralResponseDto
import com.example.proyecto_appmovil.data.remote.dto.ResumenAspiranteDto
import com.example.proyecto_appmovil.data.remote.dto.RevisionDto
import com.example.proyecto_appmovil.data.remote.dto.RevisionResponseDto
import com.example.proyecto_appmovil.data.remote.dto.SubirDocumentoResponseDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface InscripcionesApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequestDto): LoginResponseDto

    @POST("auth/refresh")
    fun refresh(@Body request: RefreshTokenRequestDto): Call<RefreshTokenResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshTokenRequestDto): Response<Unit>

    @PATCH("auth/password")
    suspend fun cambiarPassword(@Body request: CambiarPasswordRequestDto): Response<Unit>

    @GET("catalogos/carreras")
    suspend fun getCarreras(): List<CarreraDto>

    @GET("aspirantes/me")
    suspend fun getMiAspirante(): AspiranteDto

    @GET("aspirantes/me/resumen")
    suspend fun getMiResumen(): ResumenAspiranteDto

    @PUT("aspirantes/me")
    suspend fun actualizarMiAspirante(@Body request: ActualizarAspiranteRequestDto): AspiranteDto

    @GET("aspirantes/me/documentos")
    suspend fun getDocumentos(): List<DocumentoItemDto>

    @Multipart
    @POST("aspirantes/me/documentos")
    suspend fun subirDocumento(
        @Part archivo: MultipartBody.Part,
        @Part("tipoCodigo") tipoCodigo: RequestBody,
        @Part("hashSha256") hashSha256: RequestBody
    ): SubirDocumentoResponseDto

    @DELETE("documentos/{id}")
    suspend fun eliminarDocumento(@Path("id") id: Long): Response<Unit>

    @GET("notificaciones")
    suspend fun getNotificaciones(
        @Query("leida") leida: Boolean? = null,
        @Query("afterId") afterId: Long? = null
    ): List<NotificacionItemDto>

    @PATCH("notificaciones/{id}/leida")
    suspend fun marcarNotificacionLeida(@Path("id") id: Long): Response<Unit>

    @PATCH("notificaciones/leer-todas")
    suspend fun marcarTodasNotificacionesLeidas(): Response<Unit>

    @DELETE("notificaciones/{id}")
    suspend fun eliminarNotificacion(@Path("id") id: Long): Response<Unit>

    @GET("fechas-importantes")
    suspend fun getFechasImportantes(): List<FechaImportanteDto>

    @POST("admin/fechas-importantes")
    suspend fun crearFechaImportante(@Body request: CrearFechaImportanteDto): FechaImportanteDto

    @PUT("admin/fechas-importantes/{id}")
    suspend fun actualizarFechaImportante(
        @Path("id") id: Long,
        @Body request: CrearFechaImportanteDto
    ): FechaImportanteDto

    @DELETE("admin/fechas-importantes/{id}")
    suspend fun eliminarFechaImportante(@Path("id") id: Long): Response<Unit>

    @GET("admin/aspirantes")
    suspend fun getAdminAspirantes(
        @Query("estado") estado: String? = null,
        @Query("q") q: String? = null
    ): List<AspiranteDto>

    @GET("admin/aspirantes/{id}")
    suspend fun getAdminAspiranteDetalle(@Path("id") id: Long): AspiranteDto

    @GET("admin/aspirantes/{id}/documentos")
    suspend fun getDocumentosAspirante(@Path("id") id: Long): List<DocumentoItemDto>

    @Streaming
    @Headers("Accept: application/pdf")
    @GET("admin/documentos/{id}/archivo")
    suspend fun descargarArchivoDocumento(@Path("id") id: Long): Response<ResponseBody>

    @POST("admin/documentos/{id}/revision")
    suspend fun revisarDocumento(
        @Path("id") id: Long,
        @Body revision: RevisionDto
    ): RevisionResponseDto

    @POST("admin/aspirantes/{id}/aprobar")
    suspend fun aprobarAspirante(@Path("id") id: Long): AspiranteDto

    @POST("aspirantes/me/preguntas")
    suspend fun crearPregunta(@Body request: CrearPreguntaDto): PreguntaDto

    @GET("aspirantes/me/preguntas")
    suspend fun getMisPreguntas(): List<PreguntaDto>

    @GET("admin/preguntas")
    suspend fun getPreguntasAdmin(
        @Query("categoria") categoria: String? = null,
        @Query("estado") estado: String? = null
    ): List<PreguntaDto>

    @POST("admin/preguntas/{id}/responder")
    suspend fun responderPregunta(
        @Path("id") id: Long,
        @Body request: ResponderPreguntaDto
    ): PreguntaDto

    @POST("admin/notificaciones/aviso-general")
    suspend fun enviarAvisoGeneral(@Body request: AvisoGeneralDto): AvisoGeneralResponseDto

    @GET("notificaciones")
    suspend fun getNotificacionesNuevas(
        @Query("leida") leida: Boolean? = false,
        @Query("afterId") afterId: Long
    ): List<NotificacionItemDto>
}
