package com.example.proyecto_appmovil.data.remote.dto

data class DocumentoDto(
    val id: String,
    val aspiranteId: String,
    val tipo: String,
    val nombreArchivo: String?,
    val estado: String,
    val observaciones: String?
)
