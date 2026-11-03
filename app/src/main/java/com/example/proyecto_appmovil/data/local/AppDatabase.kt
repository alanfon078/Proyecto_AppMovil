package com.example.proyecto_appmovil.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.proyecto_appmovil.data.local.dao.*
import com.example.proyecto_appmovil.data.local.entity.*

@Database(
    entities = [
        UsuarioEntity::class,
        AspiranteEntity::class,
        DocumentoEntity::class,
        ComprobantePagoEntity::class,
        NotificacionEntity::class,
        FechaImportanteEntity::class,
        CarreraEntity::class,
        EstadoEntity::class,
        MunicipioEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun aspiranteDao(): AspiranteDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun comprobantePagoDao(): ComprobantePagoDao
    abstract fun fechaImportanteDao(): FechaImportanteDao
    abstract fun catalogosDao(): CatalogosDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "itsur_inscripciones.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
