package com.example.proyecto_appmovil.ui.screens.documents

import androidx.lifecycle.ViewModel
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository

class DocumentsViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    val inscripcionesRepo: InscripcionesRepository get() = repository
}
