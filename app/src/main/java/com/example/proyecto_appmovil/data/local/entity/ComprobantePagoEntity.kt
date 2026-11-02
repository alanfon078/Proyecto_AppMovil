package com.example.proyecto_appmovil.data.local.entity

// TODO: Agregar anotación @Entity(tableName = "comprobantes_pago") al integrar Room
data class ComprobantePagoEntity(
    val id: String,
    val aspiranteId: String,
    val folio: String,
    val monto: Double,
    val fechaPago: String,
    val comprobanteUri: String,
    val validado: Boolean
)
