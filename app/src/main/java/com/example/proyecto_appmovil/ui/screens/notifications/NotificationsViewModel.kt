package com.example.proyecto_appmovil.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.Notificacion
import com.example.proyecto_appmovil.ui.resultadoSolicitud
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
    val isLoading: Boolean = false,
    val mensajeError: String? = null
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
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            resultadoSolicitud { repository.getNotificaciones() to repository.getFechasImportantes() }
                .onSuccess { (notificaciones, fechas) ->
                    _uiState.update {
                        it.copy(notificaciones = notificaciones, fechas = fechas, isLoading = false)
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudieron cargar los avisos")
                    }
                }
        }
    }

    fun setTab(index: Int) = _uiState.update { it.copy(tabSeleccionada = index) }
    fun toggleSoloNoLeidas() = _uiState.update { it.copy(soloNoLeidas = !it.soloNoLeidas) }

    fun marcarComoLeida(id: String) {
        viewModelScope.launch {
            resultadoSolicitud { repository.marcarNotificacionLeida(id) }
                .onSuccess {
                    _uiState.update { estado ->
                        estado.copy(
                            notificaciones = estado.notificaciones.map { notif ->
                                if (notif.id == id) notif.copy(leida = true) else notif
                            },
                            mensajeError = null
                        )
                    }
                }.onFailure { ex ->
                    _uiState.update { it.copy(mensajeError = ex.message ?: "No se pudo marcar el aviso como leído") }
                }
        }
    }
}
