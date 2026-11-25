package com.example.proyecto_appmovil.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.proyecto_appmovil.InscripcionesApp
import com.example.proyecto_appmovil.util.NotificationHelper
import kotlinx.coroutines.CancellationException

class NotificacionesSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as InscripcionesApp).container
        return try {
            container.inscripcionesRepository.sincronizarNotificaciones { notificacion ->
                NotificationHelper.show(
                    applicationContext,
                    notificacion.id,
                    notificacion.titulo,
                    notificacion.mensaje
                )
            }
            Result.success()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
