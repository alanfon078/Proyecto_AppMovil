package com.example.proyecto_appmovil.data.local.dao

import com.example.proyecto_appmovil.data.local.entity.DocumentoEntity

// TODO: Agregar anotación @Dao al integrar Room
interface DocumentoDao {
    // TODO: Implementar queries @Insert, @Update, @Query
    suspend fun getDocumentos(): List<DocumentoEntity>
    suspend fun insertDocumento(documento: DocumentoEntity)
}
