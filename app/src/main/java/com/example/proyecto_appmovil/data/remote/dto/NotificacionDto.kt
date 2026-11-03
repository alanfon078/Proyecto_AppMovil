package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificacionItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("idUsuario")
    val idUsuario: Long,
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("mensaje")
    val mensaje: String,
    @SerializedName("leida")
    val leida: Boolean = false,
    @SerializedName("fechaEnvio")
    val fechaEnvio: String
)

data class MarcarLeidaRequestDto(
    @SerializedName("leida")
    val leida: Boolean = true
)
