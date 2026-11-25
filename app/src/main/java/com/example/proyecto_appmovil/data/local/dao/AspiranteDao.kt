package com.example.proyecto_appmovil.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.proyecto_appmovil.data.local.entity.AspiranteEntity

@Dao
interface AspiranteDao {
    @Upsert
    suspend fun insertOrUpdate(aspirante: AspiranteEntity)

    @Query("UPDATE aspirantes SET estadoInscripcion = :estado WHERE idAspirante = :id")
    suspend fun actualizarEstado(id: Long, estado: String)
}
