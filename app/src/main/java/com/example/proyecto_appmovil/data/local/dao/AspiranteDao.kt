package com.example.proyecto_appmovil.data.local.dao

import com.example.proyecto_appmovil.data.local.entity.AspiranteEntity

// TODO: Agregar anotación @Dao al integrar Room
interface AspiranteDao {
    // TODO: Implementar queries @Insert, @Update, @Query
    suspend fun getAspirante(): AspiranteEntity?
    suspend fun insertAspirante(aspirante: AspiranteEntity)
}
