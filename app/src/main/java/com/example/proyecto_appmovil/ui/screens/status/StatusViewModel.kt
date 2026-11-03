package com.example.proyecto_appmovil.ui.screens.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatusUiState(
    val aspirante: Aspirante? = null,
    val documentos: List<Documento> = emptyList(),
    val etapaActual: Int = 3, // 1: Registro, 2: Documentos, 3: Revisión, 4: Pago, 5: Conclusión
    val isLoading: Boolean = false
)

class StatusViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        cargarEstatus()
    }

    fun cargarEstatus() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val asp = repository.getAspiranteActual()
            val docs = repository.getDocumentos()
            val pago = repository.getComprobantePago()

            val etapa = when {
                pago.validado -> 5
                docs.all { it.estado.name == "VALIDADO" } -> 4
                docs.any { it.estado.name == "SUBIDO" } -> 3
                docs.any { it.estado.name == "VALIDADO" } -> 2
                else -> 1
            }

            _uiState.update {
                it.copy(
                    aspirante = asp,
                    documentos = docs,
                    etapaActual = etapa,
                    isLoading = false
                )
            }
        }
    }
}
