package com.example.proyecto_appmovil.data.local

import com.example.proyecto_appmovil.data.local.dao.AspiranteDao
import com.example.proyecto_appmovil.data.local.dao.ComprobantePagoDao
import com.example.proyecto_appmovil.data.local.dao.DocumentoDao
import com.example.proyecto_appmovil.data.local.dao.NotificacionDao

// TODO: Extender de RoomDatabase() y agregar anotación @Database(...) al integrar Room
abstract class AppDatabase {
    abstract fun aspiranteDao(): AspiranteDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun comprobantePagoDao(): ComprobantePagoDao
}
