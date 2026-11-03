package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [
        Index(value = ["correo"], unique = true),
        Index(value = ["nombreUsuario"], unique = true)
    ]
)
data class UsuarioEntity(
    @PrimaryKey
    val idUsuario: Long,
    val nombre: String,
    val apellidos: String,
    val nombreUsuario: String,
    val correo: String,
    val rol: String,
    val activo: Boolean = true
)
