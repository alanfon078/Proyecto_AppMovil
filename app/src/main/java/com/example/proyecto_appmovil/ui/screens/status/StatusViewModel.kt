package com.example.proyecto_appmovil.ui.screens.status

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StatusUiState(
    val aspirante: Aspirante? = null,
    val documentos: List<Documento> = emptyList(),
    val etapaActual: Int = 0,
    val isLoading: Boolean = false,
    val mensajeError: String? = null
)

class StatusViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState())
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    fun cargarEstatus() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            resultadoSolicitud { repository.getAspiranteActual() to repository.getDocumentos() }
                .onSuccess { (aspirante, documentos) ->
                    val requeridos = TipoDocumento.entries.filter(TipoDocumento::obligatorio)
                    val porTipo = documentos.associateBy(Documento::tipo)
                    val todosAprobados = requeridos.all { porTipo[it]?.estado == EstadoDocumento.VALIDADO }
                    val todosSubidos = requeridos.all { porTipo[it]?.nombreArchivo != null }
                    val etapa = when {
                        aspirante.estadoInscripcion == EstadoInscripcion.ACEPTADO && todosAprobados -> 4
                        todosSubidos -> 3
                        else -> 2
                    }
                    _uiState.update {
                        it.copy(aspirante = aspirante, documentos = documentos, etapaActual = etapa, isLoading = false)
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cargar el estatus")
                    }
                }
        }
    }
}
