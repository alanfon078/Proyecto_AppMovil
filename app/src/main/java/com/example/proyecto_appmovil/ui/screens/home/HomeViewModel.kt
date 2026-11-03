package com.example.proyecto_appmovil.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val aspirante: Aspirante? = null,
    val documentosSubidosCount: Int = 0,
    val totalDocumentosCount: Int = 4,
    val notificacionesNoLeidasCount: Int = 0,
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val aspirante = repository.getAspiranteActual()
            val docs = repository.getDocumentos()
            val notifs = repository.getNotificaciones()

            val subidos = docs.count { it.estado.name == "VALIDADO" || it.estado.name == "SUBIDO" }
            val noLeidas = notifs.count { !it.leida }

            _uiState.update {
                it.copy(
                    aspirante = aspirante,
                    documentosSubidosCount = subidos,
                    totalDocumentosCount = docs.size,
                    notificacionesNoLeidasCount = noLeidas,
                    isLoading = false
                )
            }
        }
    }
}
