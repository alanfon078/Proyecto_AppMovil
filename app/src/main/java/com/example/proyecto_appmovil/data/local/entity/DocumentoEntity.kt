package com.example.proyecto_appmovil.data.local.entity

// TODO: Agregar anotación @Entity(tableName = "documentos") al integrar Room
data class DocumentoEntity(
    val id: String,
    val aspiranteId: String,
    val tipo: String,
    val nombreArchivo: String?,
    val uriString: String?,
    val estado: String,
    val observaciones: String
)
