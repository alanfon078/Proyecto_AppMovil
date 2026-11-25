package com.example.proyecto_appmovil.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyecto_appmovil.di.DefaultAppContainer
import com.example.proyecto_appmovil.util.NotificationHelper

class NotificacionesSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val container = DefaultAppContainer(applicationContext)
        val session = container.sessionManager
        if (!session.isLoggedIn() || session.getUserRole()?.lowercase() != "aspirante") {
            return Result.success()
        }
        return try {
            val afterId = session.getUltimoIdNotificado()
            val nuevas = container.inscripcionesRepository.getNotificacionesNuevas(afterId)
            nuevas.forEachIndexed { index, notificacion ->
                NotificationHelper.show(
                    applicationContext,
                    (notificacion.id.toIntOrNull() ?: (1000 + index)),
                    notificacion.titulo,
                    notificacion.mensaje
                )
            }
            val maxId = nuevas.maxOfOrNull { it.id.toLongOrNull() ?: 0L } ?: afterId
            if (maxId > afterId) session.saveUltimoIdNotificado(maxId)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
