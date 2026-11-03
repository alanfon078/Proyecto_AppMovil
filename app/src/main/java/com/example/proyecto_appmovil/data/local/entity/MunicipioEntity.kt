package com.example.proyecto_appmovil.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "municipios",
    foreignKeys = [
        ForeignKey(
            entity = EstadoEntity::class,
            parentColumns = ["idEstado"],
            childColumns = ["idEstado"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["idEstado"])
    ]
)
data class MunicipioEntity(
    @PrimaryKey
    val idMunicipio: Int,
    val idEstado: Int,
    val nombre: String
)
