package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.FechaImportanteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FechaImportanteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fechas: List<FechaImportanteEntity>)

    @Query("SELECT * FROM fechas_importantes WHERE activa = 1 ORDER BY fechaLimite ASC")
    fun getFechasVigentesFlow(): Flow<List<FechaImportanteEntity>>

    @Query("DELETE FROM fechas_importantes")
    suspend fun clearAll()
}
