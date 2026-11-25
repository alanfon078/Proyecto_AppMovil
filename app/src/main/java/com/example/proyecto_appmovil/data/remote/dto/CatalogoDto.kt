package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CarreraDto(
    @SerializedName("id_carrera") val idCarrera: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("activa") val activa: Boolean
)

data class FechaImportanteDto(
    @SerializedName("id_fecha") val idFecha: Long,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("tipo") val tipo: String,
    @SerializedName("fecha_limite") val fechaLimite: String,
    @SerializedName("dias_restantes") val diasRestantes: Int,
    @SerializedName("id_carrera") val idCarrera: Int?,
    @SerializedName("activa") val activa: Boolean
)

data class CrearFechaImportanteDto(
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fecha_limite") val fechaLimite: String
)
