package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "estados")
data class EstadoEntity(
    @PrimaryKey
    val idEstado: Int,
    val nombre: String,
    val abreviatura: String
)
