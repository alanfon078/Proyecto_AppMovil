package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.DocumentoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(documento: DocumentoEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(documentos: List<DocumentoEntity>)

    @Update
    suspend fun update(documento: DocumentoEntity)

    @Query("DELETE FROM documentos WHERE idLocal = :idLocal")
    suspend fun deleteById(idLocal: Long)

    @Query("SELECT * FROM documentos ORDER BY idTipoDocumento ASC")
    fun getDocumentosFlow(): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE idAspirante = :idAspirante ORDER BY idTipoDocumento ASC")
    fun getDocumentosPorAspiranteFlow(idAspirante: Long): Flow<List<DocumentoEntity>>

    @Query("SELECT * FROM documentos WHERE tipoCodigo = :tipoCodigo LIMIT 1")
    fun getDocumentoPorTipoFlow(tipoCodigo: String): Flow<DocumentoEntity?>

    @Query("SELECT * FROM documentos WHERE syncPendiente = 1")
    suspend fun getPendientesSync(): List<DocumentoEntity>

    @Query("DELETE FROM documentos")
    suspend fun clearAll()
}
