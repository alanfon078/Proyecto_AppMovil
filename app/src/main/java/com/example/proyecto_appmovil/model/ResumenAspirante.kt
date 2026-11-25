package com.example.proyecto_appmovil.model

data class ResumenAspirante(
    val aspirante: Aspirante,
    val requeridos: Int,
    val subidos: Int,
    val aprobados: Int,
    val pendientes: Int,
    val rechazados: Int,
    val notificacionesNoLeidas: Int
)
