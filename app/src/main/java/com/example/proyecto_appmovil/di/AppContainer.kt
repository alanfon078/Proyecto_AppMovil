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

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        AppDatabase.getDatabase(context)
    }

    override val sessionManager: SessionManager by lazy {
        SessionManager(context)
    }

    override val apiService: InscripcionesApiService by lazy {
        RetrofitClient.createApiService(
            tokenProvider = { sessionManager.getAuthToken() }
        )
    }

    override val inscripcionesRepository: InscripcionesRepository by lazy {
        InscripcionesRepositoryImpl(
            aspiranteDao = database.aspiranteDao(),
            documentoDao = database.documentoDao(),
            comprobantePagoDao = database.comprobantePagoDao(),
            notificacionDao = database.notificacionDao(),
            fechaImportanteDao = database.fechaImportanteDao(),
            catalogosDao = database.catalogosDao(),
            usuarioDao = database.usuarioDao(),
            apiService = apiService,
            sessionManager = sessionManager
        )
    }
}
