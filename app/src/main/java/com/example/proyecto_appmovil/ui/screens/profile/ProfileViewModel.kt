package com.example.proyecto_appmovil.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository

class ProfileViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    val inscripcionesRepo: InscripcionesRepository get() = repository
}
