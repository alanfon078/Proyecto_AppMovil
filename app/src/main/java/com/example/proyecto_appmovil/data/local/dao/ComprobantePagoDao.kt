package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.ComprobantePagoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ComprobantePagoDao {
    @Upsert
    suspend fun insertOrUpdate(pago: ComprobantePagoEntity): Long

    @Query("SELECT * FROM comprobantes_pago ORDER BY idPago DESC LIMIT 1")
    fun getPagoFlow(): Flow<ComprobantePagoEntity?>

    @Query("SELECT * FROM comprobantes_pago WHERE idAspirante = :idAspirante ORDER BY idPago DESC LIMIT 1")
    fun getPagoPorAspiranteFlow(idAspirante: Long): Flow<ComprobantePagoEntity?>

    @Query("SELECT * FROM comprobantes_pago ORDER BY idPago DESC LIMIT 1")
    suspend fun getPagoSync(): ComprobantePagoEntity?

    @Delete
    suspend fun delete(pago: ComprobantePagoEntity)

    @Query("DELETE FROM comprobantes_pago")
    suspend fun clearAll()
}
