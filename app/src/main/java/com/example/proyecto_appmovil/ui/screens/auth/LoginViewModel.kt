package com.example.proyecto_appmovil.ui.screens.auth

import androidx.lifecycle.ViewModel
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository

class LoginViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    val inscripcionesRepo: InscripcionesRepository get() = repository
}
