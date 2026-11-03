package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequestDto(
    @SerializedName("email")
    val email: String,
    @SerializedName("contrasena")
    val contrasena: String
)

data class LoginResponseDto(
    @SerializedName("token")
    val token: String,
    @SerializedName("refreshToken")
    val refreshToken: String? = null,
    @SerializedName("usuario")
    val usuario: UserDto,
    @SerializedName("aspirante")
    val aspirante: AspiranteDto? = null
)

data class RegisterRequestDto(
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("curp")
    val curp: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("idCarrera")
    val idCarrera: Int,
    @SerializedName("contrasena")
    val contrasena: String
)

data class UserDto(
    @SerializedName("idUsuario")
    val idUsuario: Long,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("apellidos")
    val apellidos: String,
    @SerializedName("nombreUsuario")
    val nombreUsuario: String,
    @SerializedName("correo")
    val correo: String,
    @SerializedName("rol")
    val rol: String,
    @SerializedName("activo")
    val activo: Boolean = true
)

data class AspiranteDto(
    @SerializedName("idAspirante")
    val idAspirante: Long,
    @SerializedName("idUsuario")
    val idUsuario: Long,
    @SerializedName("folio")
    val folio: String,
    @SerializedName("curp")
    val curp: String,
    @SerializedName("telefono")
    val telefono: String,
    @SerializedName("direccion")
    val direccion: String? = null,
    @SerializedName("idEstado")
    val idEstado: Int? = null,
    @SerializedName("idMunicipio")
    val idMunicipio: Int? = null,
    @SerializedName("idCarrera")
    val idCarrera: Int? = null,
    @SerializedName("promedioBachillerato")
    val promedioBachillerato: Double? = 0.0,
    @SerializedName("estadoInscripcion")
    val estadoInscripcion: String = "PENDIENTE",
    @SerializedName("carreraNombre")
    val carreraNombre: String? = null
)
