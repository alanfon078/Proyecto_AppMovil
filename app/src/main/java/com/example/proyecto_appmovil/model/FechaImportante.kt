package com.example.proyecto_appmovil.model

data class FechaImportante(
    val id: String,
    val titulo: String,
    val descripcion: String,
    val fechaLimite: String,
    val diasRestantes: Int,
    val tipo: String = "limite_entrega" // limite_entrega, examen, propedeutico, pago
)
