package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carreras")
data class CarreraEntity(
    @PrimaryKey val idCarrera: Int,
    val nombre: String,
    val activa: Boolean
)
