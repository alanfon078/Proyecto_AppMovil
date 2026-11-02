package com.example.proyecto_appmovil.di

import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.data.repository.InscripcionesRepositoryImpl

interface AppContainer {
    val inscripcionesRepository: InscripcionesRepository
}

class DefaultAppContainer : AppContainer {
    // TODO: Inicializar Room Database y Retrofit ApiService aquí
    override val inscripcionesRepository: InscripcionesRepository by lazy {
        InscripcionesRepositoryImpl()
    }
}
