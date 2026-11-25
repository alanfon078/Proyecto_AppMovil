package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("identificador") val identificador: String,
    @SerializedName("contrasena") val contrasena: String
)

data class LoginResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String,
    @SerializedName("usuario") val usuario: UserDto,
    @SerializedName("aspirante") val aspirante: AspiranteDto?
)

data class RefreshTokenRequestDto(
    @SerializedName("refresh_token") val refreshToken: String
)

data class RefreshTokenResponseDto(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("refresh_token") val refreshToken: String
)

data class RegisterRequestDto(
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("curp") val curp: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("id_carrera") val idCarrera: Int,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("estado_residencia") val estadoResidencia: String,
    @SerializedName("municipio_residencia") val municipioResidencia: String
)

data class DisponibilidadUsuarioDto(
    @SerializedName("disponible") val disponible: Boolean
)

data class CambiarPasswordRequestDto(
    @SerializedName("contrasena_actual") val contrasenaActual: String,
    @SerializedName("contrasena_nueva") val contrasenaNueva: String
)

data class ActualizarAspiranteRequestDto(
    @SerializedName("telefono") val telefono: String,
    @SerializedName("direccion") val direccion: String,
    @SerializedName("estado_residencia") val estadoResidencia: String,
    @SerializedName("municipio_residencia") val municipioResidencia: String
)

data class UserDto(
    @SerializedName("id_usuario") val idUsuario: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("nombre_usuario") val nombreUsuario: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("activo") val activo: Boolean
)

data class AspiranteDto(
    @SerializedName("id_aspirante") val idAspirante: Long,
    @SerializedName("id_usuario") val idUsuario: Long,
    @SerializedName("folio") val folio: String,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("curp") val curp: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("direccion") val direccion: String?,
    @SerializedName("estado_residencia") val estadoResidencia: String?,
    @SerializedName("municipio_residencia") val municipioResidencia: String?,
    @SerializedName("id_carrera") val idCarrera: Int?,
    @SerializedName("carrera") val carreraNombre: String?,
    @SerializedName("promedio_bachillerato") val promedioBachillerato: Double?,
    @SerializedName("estado_inscripcion") val estadoInscripcion: String
)

data class ResumenAspiranteDto(
    @SerializedName("aspirante") val aspirante: AspiranteDto,
    @SerializedName("documentos") val documentos: ResumenDocumentosDto,
    @SerializedName("notificaciones_no_leidas") val notificacionesNoLeidas: Int
)

data class ResumenDocumentosDto(
    @SerializedName("requeridos") val requeridos: Int,
    @SerializedName("subidos") val subidos: Int,
    @SerializedName("aprobados") val aprobados: Int,
    @SerializedName("pendientes") val pendientes: Int,
    @SerializedName("rechazados") val rechazados: Int
)
