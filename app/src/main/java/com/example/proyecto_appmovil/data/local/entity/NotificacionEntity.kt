package com.example.proyecto_appmovil.data.local.entity

// TODO: Agregar anotación @Entity(tableName = "notificaciones") al integrar Room
data class NotificacionEntity(
    val id: String,
    val aspiranteId: String,
    val titulo: String,
    val mensaje: String,
    val fecha: String,
    val leida: Boolean
)
