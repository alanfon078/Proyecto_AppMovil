package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "comprobantes_pago",
    foreignKeys = [
        ForeignKey(
            entity = AspiranteEntity::class,
            parentColumns = ["idAspirante"],
            childColumns = ["idAspirante"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["idAspirante"])
    ]
)
data class ComprobantePagoEntity(
    @PrimaryKey(autoGenerate = true)
    val idPago: Long = 0,
    val idRemoto: Long? = null,
    val idAspirante: Long,
    val folioPago: String,
    val monto: Double,
    val fechaPago: String,
    val rutaComprobanteLocal: String? = null,
    val estado: String = "PENDIENTE",
    val observaciones: String? = null
)
