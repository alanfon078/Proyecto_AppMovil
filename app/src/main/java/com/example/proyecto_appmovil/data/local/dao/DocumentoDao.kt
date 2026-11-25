package com.example.proyecto_appmovil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyecto_appmovil.data.local.entity.DocumentoEntity

@Dao
interface DocumentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documentos: List<DocumentoEntity>)

    @Query("DELETE FROM documentos WHERE idRemoto = :id")
    suspend fun deleteByRemoteId(id: Long)

    @Query("DELETE FROM documentos WHERE idAspirante = :id AND tipoCodigo = :tipoCodigo")
    suspend fun deleteByType(id: Long, tipoCodigo: String)

    @Query("DELETE FROM documentos")
    suspend fun clearAll()
}
