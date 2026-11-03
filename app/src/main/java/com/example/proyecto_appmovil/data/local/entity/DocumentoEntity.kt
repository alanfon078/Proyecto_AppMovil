package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = AspiranteEntity::class,
            parentColumns = ["idAspirante"],
            childColumns = ["idAspirante"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["idAspirante"]),
        Index(value = ["tipoCodigo"])
    ]
)
data class DocumentoEntity(
    @PrimaryKey(autoGenerate = true)
    val idLocal: Long = 0,
    val idRemoto: Long? = null,
    val idAspirante: Long,
    val idTipoDocumento: Int,
    val tipoCodigo: String,
    val rutaLocal: String? = null,
    val nombreOriginal: String? = null,
    val tamanoBytes: Long = 0L,
    val mimeType: String? = null,
    val version: Int = 1,
    val estado: String = "PENDIENTE",
    val observaciones: String? = null,
    val syncPendiente: Boolean = false
)
