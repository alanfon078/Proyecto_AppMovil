package com.example.proyecto_appmovil.data.local.dao

import androidx.room.*
import com.example.proyecto_appmovil.data.local.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notificaciones: List<NotificacionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notificacion: NotificacionEntity): Long

    @Query("""
        SELECT * FROM notificaciones 
        WHERE (:soloNoLeidas = 0 OR leida = 0) 
        ORDER BY idNotificacion DESC
    """)
    fun getNotificacionesFlow(soloNoLeidas: Boolean): Flow<List<NotificacionEntity>>

    @Query("SELECT COUNT(*) FROM notificaciones WHERE leida = 0")
    fun countNoLeidasFlow(): Flow<Int>

    @Query("UPDATE notificaciones SET leida = 1 WHERE idNotificacion = :id OR idRemoto = :id")
    suspend fun marcarLeida(id: Long)

    @Query("DELETE FROM notificaciones WHERE idNotificacion = :id OR idRemoto = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM notificaciones")
    suspend fun clearAll()
}
