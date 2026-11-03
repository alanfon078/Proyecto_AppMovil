package com.example.proyecto_appmovil.ui.screens.payment

import androidx.lifecycle.ViewModel
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository

class PaymentViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    val inscripcionesRepo: InscripcionesRepository get() = repository
}
