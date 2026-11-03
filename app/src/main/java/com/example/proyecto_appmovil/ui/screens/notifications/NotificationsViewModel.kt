package com.example.proyecto_appmovil.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.Notificacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val fechas: List<FechaImportante> = emptyList(),
    val tabSeleccionada: Int = 0, // 0: Avisos, 1: Fechas límite
    val soloNoLeidas: Boolean = false,
    val isLoading: Boolean = false
)

class NotificationsViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val notifs = repository.getNotificaciones()
            val fechas = repository.getFechasImportantes()
            _uiState.update {
                it.copy(
                    notificaciones = notifs,
                    fechas = fechas,
                    isLoading = false
                )
            }
        }
    }

    fun setTab(index: Int) = _uiState.update { it.copy(tabSeleccionada = index) }
    fun toggleSoloNoLeidas() = _uiState.update { it.copy(soloNoLeidas = !it.soloNoLeidas) }

    fun marcarComoLeida(id: String) {
        viewModelScope.launch {
            repository.marcarNotificacionLeida(id)
            val updated = repository.getNotificaciones()
            _uiState.update { it.copy(notificaciones = updated) }
        }
    }
}
