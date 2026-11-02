package com.example.proyecto_appmovil

import android.app.Application
import com.example.proyecto_appmovil.di.AppContainer
import com.example.proyecto_appmovil.di.DefaultAppContainer

class InscripcionesApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer()
    }
}
