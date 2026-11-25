package com.example.proyecto_appmovil.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val aspirante: Aspirante? = null,
    val documentosSubidosCount: Int = 0,
    val totalDocumentosCount: Int = 0,
    val notificacionesNoLeidasCount: Int = 0,
    val respuestasNoLeidasCount: Int = 0,
    val isLoading: Boolean = false,
    val mensajeError: String? = null
)

class HomeViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observarNotificaciones().collect { avisos ->
                val noLeidas = avisos.filterNot { it.leida }
                _uiState.update {
                    it.copy(
                        notificacionesNoLeidasCount = noLeidas.size,
                        respuestasNoLeidasCount = noLeidas.count { aviso -> aviso.tipo == "duda" }
                    )
                }
            }
        }
    }

    fun cargarDatos() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            resultadoSolicitud { repository.getMiResumen().also { repository.getNotificaciones() } }
                .onSuccess { resumen ->
                    _uiState.update {
                        it.copy(
                            aspirante = resumen.aspirante,
                            documentosSubidosCount = resumen.subidos - resumen.rechazados,
                            totalDocumentosCount = resumen.requeridos,
                            isLoading = false
                        )
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cargar el resumen")
                    }
                }
        }
    }
}
