package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DocumentoItemDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("idAspirante")
    val idAspirante: Long,
    @SerializedName("idTipoDocumento")
    val idTipoDocumento: Int,
    @SerializedName("tipoCodigo")
    val tipoCodigo: String,
    @SerializedName("url")
    val url: String? = null,
    @SerializedName("nombreOriginal")
    val nombreOriginal: String,
    @SerializedName("tamanoBytes")
    val tamanoBytes: Long = 0L,
    @SerializedName("mimeType")
    val mimeType: String? = null,
    @SerializedName("version")
    val version: Int = 1,
    @SerializedName("estado")
    val estado: String = "PENDIENTE",
    @SerializedName("observaciones")
    val observaciones: String? = null
)

data class SubirDocumentoResponseDto(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("mensaje")
    val mensaje: String,
    @SerializedName("documento")
    val documento: DocumentoItemDto? = null
)

data class RevisionDto(
    @SerializedName("estado")
    val estado: String,
    @SerializedName("observaciones")
    val observaciones: String
)
