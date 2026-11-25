package com.example.proyecto_appmovil.di

import android.content.Context
import com.example.proyecto_appmovil.data.local.AppDatabase
import com.example.proyecto_appmovil.data.local.session.SessionManager
import com.example.proyecto_appmovil.data.remote.RetrofitClient
import com.example.proyecto_appmovil.data.remote.api.InscripcionesApiService
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.data.repository.InscripcionesRepositoryImpl

interface AppContainer {
    val database: AppDatabase
    val apiService: InscripcionesApiService
    val sessionManager: SessionManager
    val inscripcionesRepository: InscripcionesRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val applicationContext = context.applicationContext

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(applicationContext)
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(applicationContext)
    }

    override val apiService: InscripcionesApiService by lazy {
        RetrofitClient.createApiService(sessionManager)
    }

    override val inscripcionesRepository: InscripcionesRepository by lazy {
        InscripcionesRepositoryImpl(applicationContext, database, apiService, sessionManager)
    }
}
