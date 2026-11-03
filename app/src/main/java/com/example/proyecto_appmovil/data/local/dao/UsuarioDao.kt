package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Upsert
    suspend fun insertOrUpdate(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios LIMIT 1")
    fun getUsuarioFlow(): Flow<UsuarioEntity?>

    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun getUsuarioSync(): UsuarioEntity?

    @Query("DELETE FROM usuarios")
    suspend fun clearAll()
}
