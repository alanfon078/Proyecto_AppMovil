package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminHomeUiState(
    val aspirantes: List<Aspirante> = emptyList(),
    val filtroBusqueda: String = "",
    val aspiranteSeleccionadoParaRevision: Aspirante? = null,
    val documentosParaRevision: List<Documento> = emptyList(),
    val documentoSeleccionado: Documento? = null,
    val observacionesInput: String = "",
    val isLoading: Boolean = false,
    val cargandoDocumentos: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class AdminHomeViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    fun cargarAspirantes() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            actualizarAspirantes()
        }
    }

    fun onBusquedaChange(query: String) = _uiState.update { it.copy(filtroBusqueda = query) }

    fun abrirDialogoRevision(aspirante: Aspirante) {
        val estado = _uiState.value
        if (estado.isLoading || estado.cargandoDocumentos) return
        _uiState.update {
            it.copy(
                aspiranteSeleccionadoParaRevision = aspirante,
                documentosParaRevision = emptyList(),
                documentoSeleccionado = null,
                observacionesInput = "",
                cargandoDocumentos = true,
                mensajeError = null
            )
        }
        viewModelScope.launch {
            resultadoSolicitud { repository.getDocumentosAspirante(aspirante.id) }
                .onSuccess { documentos ->
                    val revisables = documentos.filter { it.estado == EstadoDocumento.SUBIDO }
                    _uiState.update {
                        it.copy(
                            documentosParaRevision = revisables,
                            documentoSeleccionado = revisables.singleOrNull(),
                            cargandoDocumentos = false
                        )
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(cargandoDocumentos = false, mensajeError = ex.message ?: "No se pudieron cargar los documentos")
                    }
                }
        }
    }

    fun cerrarDialogoRevision() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(aspiranteSeleccionadoParaRevision = null, mensajeError = null) }
    }

    fun seleccionarDocumento(documento: Documento) {
        _uiState.update { it.copy(documentoSeleccionado = documento, mensajeError = null) }
    }

    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observacionesInput = v) }

    fun resolverRevision(aprobado: Boolean) {
        val estado = _uiState.value
        val doc = estado.documentoSeleccionado ?: return
        if (estado.isLoading) return
        val observaciones = estado.observacionesInput.trim()
        if (!aprobado && observaciones.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Ingrese el motivo del rechazo") }
            return
        }

        _uiState.update { it.copy(isLoading = true, mensajeError = null, mensajeExito = null) }
        viewModelScope.launch {
            repository.revisarDocumento(doc.id, aprobado, observaciones)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            aspiranteSeleccionadoParaRevision = null,
                            mensajeExito = if (aprobado) "Documento aprobado" else "Documento rechazado"
                        )
                    }
                    actualizarAspirantes()
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo guardar el dictamen")
                    }
                }
        }
    }

    private suspend fun actualizarAspirantes() {
        resultadoSolicitud { repository.getAspirantesParaRevision() }
            .onSuccess { aspirantes ->
                _uiState.update { it.copy(aspirantes = aspirantes, isLoading = false) }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cargar el panel") }
            }
    }
}
