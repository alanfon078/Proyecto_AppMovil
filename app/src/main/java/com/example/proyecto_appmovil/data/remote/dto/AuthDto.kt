package com.example.proyecto_appmovil.data.remote.dto

data class LoginRequestDto(
    val email: String,
    val contrasena: String
)

data class LoginResponseDto(
    val token: String,
    val aspiranteId: String,
    val nombreCompleto: String,
    val email: String
)

data class RegisterRequestDto(
    val nombreCompleto: String,
    val curp: String,
    val email: String,
    val telefono: String,
    val carrera: String,
    val contrasena: String
)
