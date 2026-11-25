package com.example.proyecto_appmovil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.proyecto_appmovil.data.local.entity.CarreraEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogosDao {
    @Insert
    suspend fun insertCarreras(carreras: List<CarreraEntity>)

    @Query("SELECT * FROM carreras WHERE activa = 1 ORDER BY nombre ASC")
    fun getCarrerasFlow(): Flow<List<CarreraEntity>>

    @Query("DELETE FROM carreras")
    suspend fun clearCarreras()
}
