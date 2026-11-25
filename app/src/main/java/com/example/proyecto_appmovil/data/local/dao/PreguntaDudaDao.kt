package com.example.proyecto_appmovil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyecto_appmovil.data.local.entity.PreguntaDudaEntity

@Dao
interface PreguntaDudaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(preguntas: List<PreguntaDudaEntity>)

    @Query("SELECT * FROM preguntas_dudas ORDER BY fechaCreacion DESC")
    suspend fun getAll(): List<PreguntaDudaEntity>

    @Query("DELETE FROM preguntas_dudas")
    suspend fun clearAll()
}
