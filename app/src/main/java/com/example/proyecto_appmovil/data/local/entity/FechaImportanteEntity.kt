package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fechas_importantes")
data class FechaImportanteEntity(
    @PrimaryKey
    val idFecha: Int,
    val titulo: String,
    val descripcion: String,
    val tipo: String,
    val fechaLimite: String,
    val idCarrera: Int? = null,
    val activa: Boolean = true
)
