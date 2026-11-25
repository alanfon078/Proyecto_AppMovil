package com.example.proyecto_appmovil.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.proyecto_appmovil.data.local.dao.AspiranteDao
import com.example.proyecto_appmovil.data.local.dao.CatalogosDao
import com.example.proyecto_appmovil.data.local.dao.DocumentoDao
import com.example.proyecto_appmovil.data.local.dao.FechaImportanteDao
import com.example.proyecto_appmovil.data.local.dao.NotificacionDao
import com.example.proyecto_appmovil.data.local.dao.PreguntaDudaDao
import com.example.proyecto_appmovil.data.local.dao.UsuarioDao
import com.example.proyecto_appmovil.data.local.entity.AspiranteEntity
import com.example.proyecto_appmovil.data.local.entity.CarreraEntity
import com.example.proyecto_appmovil.data.local.entity.DocumentoEntity
import com.example.proyecto_appmovil.data.local.entity.FechaImportanteEntity
import com.example.proyecto_appmovil.data.local.entity.NotificacionEntity
import com.example.proyecto_appmovil.data.local.entity.PreguntaDudaEntity
import com.example.proyecto_appmovil.data.local.entity.UsuarioEntity

@Database(
    entities = [
        UsuarioEntity::class,
        AspiranteEntity::class,
        DocumentoEntity::class,
        NotificacionEntity::class,
        FechaImportanteEntity::class,
        CarreraEntity::class,
        PreguntaDudaEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun aspiranteDao(): AspiranteDao
    abstract fun documentoDao(): DocumentoDao
    abstract fun notificacionDao(): NotificacionDao
    abstract fun fechaImportanteDao(): FechaImportanteDao
    abstract fun catalogosDao(): CatalogosDao
    abstract fun preguntaDudaDao(): PreguntaDudaDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "itsur_inscripciones.db"
            ).addMigrations(migration1To2).build().also { instance = it }
        }

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE aspirantes_v2 (
                        idAspirante INTEGER NOT NULL PRIMARY KEY,
                        idUsuario INTEGER NOT NULL,
                        folio TEXT NOT NULL,
                        curp TEXT NOT NULL,
                        telefono TEXT NOT NULL,
                        direccion TEXT NOT NULL,
                        idCarrera INTEGER,
                        carreraNombre TEXT NOT NULL,
                        estadoResidencia TEXT NOT NULL,
                        municipioResidencia TEXT NOT NULL,
                        promedioBachillerato REAL,
                        estadoInscripcion TEXT NOT NULL,
                        FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO aspirantes_v2
                    SELECT a.idAspirante, a.idUsuario, a.folio, a.curp, a.telefono, a.direccion,
                        a.idCarrera, COALESCE(c.nombre, ''), COALESCE(e.nombre, ''),
                        COALESCE(m.nombre, ''), a.promedioBachillerato, a.estadoInscripcion
                    FROM aspirantes a
                    LEFT JOIN carreras c ON c.idCarrera = a.idCarrera
                    LEFT JOIN estados e ON e.idEstado = a.idEstado
                    LEFT JOIN municipios m ON m.idMunicipio = a.idMunicipio
                """.trimIndent())

                // Copiar los documentos sin la FK antigua antes de reemplazar la tabla padre.
                db.execSQL("""
                    CREATE TEMP TABLE documentos_backup AS
                    SELECT * FROM documentos
                    WHERE tipoCodigo <> 'COMPROBANTE_PAGO' AND syncPendiente = 0 AND idRemoto IS NOT NULL
                """.trimIndent())
                db.execSQL("DROP TABLE documentos")
                db.execSQL("DROP TABLE comprobantes_pago")
                db.execSQL("DROP TABLE aspirantes")
                db.execSQL("ALTER TABLE aspirantes_v2 RENAME TO aspirantes")
                db.execSQL("CREATE INDEX index_aspirantes_idUsuario ON aspirantes(idUsuario)")
                db.execSQL("CREATE UNIQUE INDEX index_aspirantes_curp ON aspirantes(curp)")
                db.execSQL("CREATE UNIQUE INDEX index_aspirantes_folio ON aspirantes(folio)")

                db.execSQL("""
                    CREATE TABLE documentos (
                        idLocal INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        idRemoto INTEGER,
                        idAspirante INTEGER NOT NULL,
                        idTipoDocumento INTEGER NOT NULL,
                        tipoCodigo TEXT NOT NULL,
                        nombreOriginal TEXT,
                        tamanoBytes INTEGER NOT NULL,
                        mimeType TEXT,
                        version INTEGER NOT NULL,
                        estado TEXT NOT NULL,
                        observaciones TEXT,
                        fechaSubida TEXT,
                        FOREIGN KEY (idAspirante) REFERENCES aspirantes(idAspirante) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO documentos
                    SELECT idLocal, idRemoto, idAspirante, idTipoDocumento, tipoCodigo,
                        nombreOriginal, tamanoBytes, mimeType, version, estado, observaciones, NULL
                    FROM documentos_backup
                """.trimIndent())
                db.execSQL("DROP TABLE documentos_backup")
                db.execSQL("CREATE INDEX index_documentos_idAspirante ON documentos(idAspirante)")
                db.execSQL("CREATE INDEX index_documentos_tipoCodigo ON documentos(tipoCodigo)")
                db.execSQL("DELETE FROM notificaciones WHERE LOWER(tipo) = 'pago'")

                db.execSQL("DROP TABLE municipios")
                db.execSQL("DROP TABLE estados")
                db.execSQL("""
                    CREATE TABLE carreras_v2 (
                        idCarrera INTEGER NOT NULL PRIMARY KEY,
                        nombre TEXT NOT NULL,
                        activa INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("INSERT INTO carreras_v2 SELECT idCarrera, nombre, activa FROM carreras")
                db.execSQL("DROP TABLE carreras")
                db.execSQL("ALTER TABLE carreras_v2 RENAME TO carreras")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS preguntas_dudas (
                        idPregunta INTEGER NOT NULL PRIMARY KEY,
                        nombreCompleto TEXT NOT NULL,
                        folio TEXT NOT NULL,
                        carrera TEXT NOT NULL,
                        categoria TEXT NOT NULL,
                        pregunta TEXT NOT NULL,
                        estado TEXT NOT NULL,
                        respuesta TEXT,
                        fechaCreacion TEXT NOT NULL,
                        fechaRespuesta TEXT
                    )
                """.trimIndent())
            }
        }
    }
}
