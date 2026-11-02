package com.example.proyecto_appmovil.data.local.dao

import com.example.proyecto_appmovil.data.local.entity.ComprobantePagoEntity

// TODO: Agregar anotación @Dao al integrar Room
interface ComprobantePagoDao {
    // TODO: Implementar queries @Insert, @Update, @Query
    suspend fun getComprobantePago(): ComprobantePagoEntity?
    suspend fun insertComprobantePago(comprobante: ComprobantePagoEntity)
}
