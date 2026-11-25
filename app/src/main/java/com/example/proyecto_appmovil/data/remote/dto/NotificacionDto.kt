package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NotificacionItemDto(
    @SerializedName("id_notificacion") val id: Long,
    @SerializedName("id_usuario") val idUsuario: Long,
    @SerializedName("id_pregunta") val idPregunta: Long? = null,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("mensaje") val mensaje: String,
    @SerializedName("leida") val leida: Boolean,
    @SerializedName("fecha_envio") val fechaEnvio: String
)
