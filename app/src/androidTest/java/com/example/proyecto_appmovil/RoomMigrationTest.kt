package com.example.proyecto_appmovil

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.proyecto_appmovil.data.local.AppDatabase
import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMigrationTest {
    @Test
    fun migraVersionDosDeF08YConservaAvisos() = verifyMigration(f09 = false)

    @Test
    fun migraVersionDosDeF09YConservaAvisosYPreguntas() = verifyMigration(f09 = true)

    private fun verifyMigration(f09: Boolean) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "migration-${UUID.randomUUID()}.db"
        val builder = Room.databaseBuilder(context, AppDatabase::class.java, name)
            .addMigrations(AppDatabase.migration1To2, AppDatabase.migration2To3)

        try {
            val initial = builder.build()
            try {
                val db = initial.openHelper.writableDatabase
                db.execSQL("INSERT INTO usuarios VALUES (1, 'Ana', 'Prueba', 'ana', 'ana@example.test', 'aspirante', 1)")
            } finally {
                initial.close()
            }
            SQLiteDatabase.openDatabase(
                context.getDatabasePath(name).path, null, SQLiteDatabase.OPEN_READWRITE
            ).use { db ->
                if (f09) {
                    db.execSQL("INSERT INTO preguntas_dudas VALUES (7, '', '', '', 'fechas', 'Pregunta', 'pendiente', NULL, '2026-11-25', NULL)")
                    db.execSQL("INSERT INTO notificaciones VALUES (4, 4, 1, 'duda', 'Aviso', 'Mensaje', 0, '2026-11-25', 7)")
                } else {
                    db.execSQL("DROP TABLE preguntas_dudas")
                    db.execSQL("DROP TABLE notificaciones")
                    db.execSQL("""
                        CREATE TABLE notificaciones (
                            idNotificacion INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            idRemoto INTEGER,
                            idUsuario INTEGER NOT NULL,
                            tipo TEXT NOT NULL,
                            titulo TEXT NOT NULL,
                            mensaje TEXT NOT NULL,
                            leida INTEGER NOT NULL,
                            fechaEnvio TEXT NOT NULL,
                            FOREIGN KEY (idUsuario) REFERENCES usuarios(idUsuario) ON DELETE CASCADE
                        )
                    """.trimIndent())
                    db.execSQL("CREATE INDEX index_notificaciones_idUsuario ON notificaciones (idUsuario)")
                    db.execSQL("INSERT INTO notificaciones VALUES (4, 4, 1, 'documento', 'Aviso', 'Mensaje', 0, '2026-11-25')")
                }
                db.version = 2
            }
            val migrated = builder.build()
            try {
                val db = migrated.openHelper.writableDatabase
                assertEquals(3, db.version)
                db.query("SELECT titulo, idPregunta FROM notificaciones WHERE idNotificacion = 4").use {
                    assertTrue(it.moveToFirst())
                    assertEquals("Aviso", it.getString(0))
                    if (f09) assertEquals(7L, it.getLong(1)) else assertNull(it.getString(1))
                }
                db.query("SELECT COUNT(*) FROM preguntas_dudas").use {
                    assertTrue(it.moveToFirst())
                    assertEquals(if (f09) 1 else 0, it.getInt(0))
                }
            } finally {
                migrated.close()
            }
        } finally {
            context.deleteDatabase(name)
        }
    }
}
