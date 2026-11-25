package com.example.proyecto_appmovil.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.proyecto_appmovil.data.local.entity.NotificacionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificacionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notificaciones: List<NotificacionEntity>)

    @Query("SELECT * FROM notificaciones ORDER BY idRemoto ASC")
    fun observarTodas(): Flow<List<NotificacionEntity>>

    @Query("UPDATE notificaciones SET leida = 1 WHERE idRemoto = :id")
    suspend fun marcarLeida(id: Long)

    @Query("UPDATE notificaciones SET leida = 1")
    suspend fun marcarTodasLeidas()

    @Query("DELETE FROM notificaciones WHERE idRemoto = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM notificaciones")
    suspend fun clearAll()
}
