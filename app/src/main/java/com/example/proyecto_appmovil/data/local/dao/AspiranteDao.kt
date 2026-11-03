package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.AspiranteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AspiranteDao {
    @Upsert
    suspend fun insertOrUpdate(aspirante: AspiranteEntity)

    @Query("SELECT * FROM aspirantes LIMIT 1")
    fun getAspiranteFlow(): Flow<AspiranteEntity?>

    @Query("SELECT * FROM aspirantes WHERE idAspirante = :idAspirante LIMIT 1")
    fun getAspirantePorIdFlow(idAspirante: Long): Flow<AspiranteEntity?>

    @Query("SELECT * FROM aspirantes LIMIT 1")
    suspend fun getAspiranteSync(): AspiranteEntity?

    @Query("SELECT * FROM aspirantes WHERE idAspirante = :idAspirante LIMIT 1")
    suspend fun getAspirantePorIdSync(idAspirante: Long): AspiranteEntity?

    @Query("""
        UPDATE aspirantes 
        SET telefono = :telefono, direccion = :direccion, idEstado = :idEstado, idMunicipio = :idMunicipio 
        WHERE idAspirante = :idAspirante
    """)
    suspend fun updateDatosPersonales(
        idAspirante: Long,
        telefono: String,
        direccion: String,
        idEstado: Int?,
        idMunicipio: Int?
    )

    @Delete
    suspend fun delete(aspirante: AspiranteEntity)

    @Query("DELETE FROM aspirantes WHERE idAspirante = :idAspirante")
    suspend fun deleteById(idAspirante: Long)

    @Query("DELETE FROM aspirantes")
    suspend fun clearAll()
}
