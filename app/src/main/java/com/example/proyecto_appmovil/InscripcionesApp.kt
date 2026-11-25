package com.example.proyecto_appmovil

import android.app.Application
import com.example.proyecto_appmovil.di.AppContainer
import com.example.proyecto_appmovil.di.DefaultAppContainer
import com.example.proyecto_appmovil.util.NotificationHelper
import com.example.proyecto_appmovil.work.NotificacionesWorkManager

class InscripcionesApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        NotificationHelper.createChannel(this)
        if (container.sessionManager.isLoggedIn() &&
            container.sessionManager.getUserRole()?.lowercase() == "aspirante"
        ) {
            NotificacionesWorkManager.schedulePeriodic(this)
        }
    }
}
