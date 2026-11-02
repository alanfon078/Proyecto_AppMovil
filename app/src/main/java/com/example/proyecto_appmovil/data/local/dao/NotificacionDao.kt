package com.example.proyecto_appmovil.data.local.dao

import com.example.proyecto_appmovil.data.local.entity.NotificacionEntity

// TODO: Agregar anotación @Dao al integrar Room
interface NotificacionDao {
    // TODO: Implementar queries @Insert, @Update, @Query
    suspend fun getNotificaciones(): List<NotificacionEntity>
    suspend fun insertNotificacion(notificacion: NotificacionEntity)
}
