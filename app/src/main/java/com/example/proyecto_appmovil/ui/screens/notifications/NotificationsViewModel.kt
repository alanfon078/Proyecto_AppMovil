package com.example.proyecto_appmovil.ui.screens.notifications

import androidx.lifecycle.ViewModel
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository

class NotificationsViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    val inscripcionesRepo: InscripcionesRepository get() = repository
}
