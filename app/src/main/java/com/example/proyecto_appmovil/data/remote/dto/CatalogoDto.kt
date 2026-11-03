package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CarreraDto(
    @SerializedName("idCarrera")
    val idCarrera: Int,
    @SerializedName("clave")
    val clave: String,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("area")
    val area: String,
    @SerializedName("activa")
    val activa: Boolean = true
)

data class EstadoDto(
    @SerializedName("idEstado")
    val idEstado: Int,
    @SerializedName("nombre")
    val nombre: String,
    @SerializedName("abreviatura")
    val abreviatura: String
)

data class MunicipioDto(
    @SerializedName("idMunicipio")
    val idMunicipio: Int,
    @SerializedName("idEstado")
    val idEstado: Int,
    @SerializedName("nombre")
    val nombre: String
)

data class FechaImportanteDto(
    @SerializedName("idFecha")
    val idFecha: Int,
    @SerializedName("titulo")
    val titulo: String,
    @SerializedName("descripcion")
    val descripcion: String,
    @SerializedName("tipo")
    val tipo: String,
    @SerializedName("fechaLimite")
    val fechaLimite: String,
    @SerializedName("idCarrera")
    val idCarrera: Int? = null,
    @SerializedName("activa")
    val activa: Boolean = true
)
