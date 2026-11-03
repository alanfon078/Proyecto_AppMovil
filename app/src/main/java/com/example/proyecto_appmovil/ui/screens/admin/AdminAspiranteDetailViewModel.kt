package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminAspiranteDetailUiState(
    val aspirante: Aspirante? = null,
    val documentos: List<Documento> = emptyList(),
    val observacionesInput: String = "",
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class AdminAspiranteDetailViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAspiranteDetailUiState())
    val uiState: StateFlow<AdminAspiranteDetailUiState> = _uiState.asStateFlow()

    fun cargarDetalle(aspiranteId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val asp = repository.getAspirantePorId(aspiranteId)
            val docs = repository.getDocumentos()
            _uiState.update {
                it.copy(
                    aspirante = asp,
                    documentos = docs,
                    isLoading = false
                )
            }
        }
    }

    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observacionesInput = v) }

    fun validarDocumento(docId: String, aprobado: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val obs = _uiState.value.observacionesInput
            repository.revisarDocumento(docId, aprobado, obs)
            val docsActualizados = _uiState.value.documentos.map { doc ->
                if (doc.id == docId) {
                    doc.copy(
                        estado = if (aprobado) EstadoDocumento.VALIDADO else EstadoDocumento.RECHAZADO,
                        observaciones = obs
                    )
                } else doc
            }
            _uiState.update {
                it.copy(
                    documentos = docsActualizados,
                    isLoading = false,
                    mensajeExito = "Documento marcado como ${if (aprobado) "VALIDADO" else "RECHAZADO"}."
                )
            }
        }
    }

    fun dictaminarAspirante(aprobado: Boolean) {
        val asp = _uiState.value.aspirante ?: return
        val obs = _uiState.value.observacionesInput.trim()

        if (!aprobado && obs.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Las observaciones son obligatorias para rechazar un expediente.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            val nuevoEstado = if (aprobado) EstadoInscripcion.ACEPTADO else EstadoInscripcion.RECHAZADO
            val actualizado = asp.copy(estadoInscripcion = nuevoEstado)
            repository.actualizarAspirante(actualizado)
            _uiState.update {
                it.copy(
                    aspirante = actualizado,
                    isLoading = false,
                    mensajeExito = "Expediente del aspirante dictaminado como: ${nuevoEstado.descripcion}"
                )
            }
        }
    }
}
