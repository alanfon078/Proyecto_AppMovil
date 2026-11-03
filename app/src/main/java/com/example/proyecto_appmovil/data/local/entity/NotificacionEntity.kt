package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notificaciones",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["idUsuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["idUsuario"])
    ]
)
data class NotificacionEntity(
    @PrimaryKey(autoGenerate = true)
    val idNotificacion: Long = 0,
    val idRemoto: Long? = null,
    val idUsuario: Long,
    val tipo: String,
    val titulo: String,
    val mensaje: String,
    val leida: Boolean = false,
    val fechaEnvio: String
)
