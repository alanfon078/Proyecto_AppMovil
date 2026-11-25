package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preguntas_dudas")
data class PreguntaDudaEntity(
    @PrimaryKey val idPregunta: Long,
    val nombreCompleto: String = "",
    val folio: String = "",
    val carrera: String = "",
    val categoria: String,
    val pregunta: String,
    val estado: String,
    val respuesta: String? = null,
    val fechaCreacion: String,
    val fechaRespuesta: String? = null
)
