package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminHomeUiState(
    val aspirantes: List<Aspirante> = emptyList(),
    val filtroBusqueda: String = "",
    val aspiranteSeleccionadoParaRevision: Aspirante? = null,
    val observacionesInput: String = "",
    val isLoading: Boolean = false,
    val mensajeExito: String? = null
)

class AdminHomeViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    init {
        cargarAspirantes()
    }

    fun cargarAspirantes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val lista = repository.getAspirantesParaRevision()
            _uiState.update { it.copy(aspirantes = lista, isLoading = false) }
        }
    }

    fun onBusquedaChange(query: String) = _uiState.update { it.copy(filtroBusqueda = query) }

    fun abrirDialogoRevision(aspirante: Aspirante) {
        _uiState.update { it.copy(aspiranteSeleccionadoParaRevision = aspirante, observacionesInput = "") }
    }

    fun cerrarDialogoRevision() {
        _uiState.update { it.copy(aspiranteSeleccionadoParaRevision = null) }
    }

    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observacionesInput = v) }

    fun resolverRevision(aprobado: Boolean) {
        val asp = _uiState.value.aspiranteSeleccionadoParaRevision ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val obs = _uiState.value.observacionesInput
            repository.revisarDocumento("doc_1", aprobado, obs)
            val updated = repository.getAspirantesParaRevision()
            _uiState.update {
                it.copy(
                    aspirantes = updated,
                    aspiranteSeleccionadoParaRevision = null,
                    isLoading = false,
                    mensajeExito = "Aspirante ${asp.nombreCompleto} ha sido ${if (aprobado) "APROBADO" else "RECHAZADO"}."
                )
            }
        }
    }
}
