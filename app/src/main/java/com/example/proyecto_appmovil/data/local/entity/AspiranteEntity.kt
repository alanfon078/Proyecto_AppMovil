package com.example.proyecto_appmovil.data.local.entity

// TODO: Agregar anotación @Entity(tableName = "aspirantes") al integrar Room
data class AspiranteEntity(
    val id: String,
    val nombreCompleto: String,
    val curp: String,
    val email: String,
    val telefono: String,
    val carrera: String,
    val folioAspirante: String,
    val estadoInscripcion: String
)
