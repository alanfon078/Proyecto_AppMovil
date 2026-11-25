package com.example.proyecto_appmovil.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DocumentoItemDto(
    @SerializedName("id_documento") val id: Long?,
    @SerializedName("id_aspirante") val idAspirante: Long,
    @SerializedName("id_tipo_documento") val idTipoDocumento: Int,
    @SerializedName("tipo_codigo") val tipoCodigo: String,
    @SerializedName("nombre_original") val nombreOriginal: String?,
    @SerializedName("tamano_bytes") val tamanoBytes: Long?,
    @SerializedName("tipo_mime") val mimeType: String?,
    @SerializedName("version") val version: Int?,
    @SerializedName("fecha_subida") val fechaSubida: String?,
    @SerializedName("estado") val estado: String,
    @SerializedName("observaciones") val observaciones: String?
)

data class SubirDocumentoResponseDto(
    @SerializedName("documento") val documento: DocumentoItemDto,
    @SerializedName("estado_inscripcion") val estadoInscripcion: String
)

data class RevisionDto(
    @SerializedName("resultado") val resultado: String,
    @SerializedName("observaciones") val observaciones: String
)

data class RevisionResponseDto(
    @SerializedName("id_revision") val idRevision: Long,
    @SerializedName("resultado") val resultado: String,
    @SerializedName("documento") val documento: DocumentoItemDto,
    @SerializedName("estado_inscripcion") val estadoInscripcion: String
)
