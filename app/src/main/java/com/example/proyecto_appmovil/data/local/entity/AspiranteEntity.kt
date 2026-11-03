package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "aspirantes",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["idUsuario"],
            childColumns = ["idUsuario"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["idUsuario"]),
        Index(value = ["curp"], unique = true),
        Index(value = ["folio"], unique = true)
    ]
)
data class AspiranteEntity(
    @PrimaryKey
    val idAspirante: Long,
    val idUsuario: Long,
    val folio: String,
    val curp: String,
    val telefono: String,
    val direccion: String,
    val idEstado: Int? = null,
    val idMunicipio: Int? = null,
    val idCarrera: Int? = null,
    val promedioBachillerato: Double = 0.0,
    val estadoInscripcion: String = "PENDIENTE"
)
