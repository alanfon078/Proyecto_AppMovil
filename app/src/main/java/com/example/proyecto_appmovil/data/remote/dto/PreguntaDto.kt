package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PreguntaDto(
    @SerializedName("idPregunta") val idPregunta: Long,
    @SerializedName("nombreCompleto") val nombreCompleto: String? = null,
    @SerializedName("folio") val folio: String? = null,
    @SerializedName("carrera") val carrera: String? = null,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("pregunta") val pregunta: String,
    @SerializedName("estado") val estado: String,
    @SerializedName("respuesta") val respuesta: String? = null,
    @SerializedName("fechaCreacion") val fechaCreacion: String,
    @SerializedName("fechaRespuesta") val fechaRespuesta: String? = null
)

data class CrearPreguntaDto(
    @SerializedName("categoria") val categoria: String,
    @SerializedName("pregunta") val pregunta: String
)

data class ResponderPreguntaDto(
    @SerializedName("respuesta") val respuesta: String
)

data class AvisoGeneralDto(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("mensaje") val mensaje: String
)

data class AvisoGeneralResponseDto(
    @SerializedName("notificados") val notificados: Int
)
