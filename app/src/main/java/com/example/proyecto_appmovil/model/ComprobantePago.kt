package com.example.proyecto_appmovil.model

data class ComprobantePago(
    val id: String = "",
    val aspiranteId: String = "",
    val folio: String = "",
    val monto: Double = 0.0,
    val fechaPago: String = "",
    val comprobanteUri: String = "",
    val validado: Boolean = false
)
