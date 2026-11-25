package com.example.proyecto_appmovil.ui.screens.admin

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
import java.io.File

data class AdminAspiranteDetailUiState(
    val aspirante: Aspirante? = null,
    val documentos: List<Documento> = emptyList(),
    val observacionesInput: String = "",
    val isLoading: Boolean = false,
    val isDownloadingPdf: Boolean = false,
    val pdfPreviewFile: File? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
) {
    val documentosAprobados: Int
        get() = TipoDocumento.entries.filter(TipoDocumento::obligatorio).count { tipo ->
            documentos.any { it.tipo == tipo && it.estado == EstadoDocumento.VALIDADO }
        }

    val documentosAprobadosFaltantes: Int
        get() = TipoDocumento.entries.count(TipoDocumento::obligatorio) - documentosAprobados

    val puedeAprobar: Boolean
        get() = aspirante != null && aspirante.estadoInscripcion != EstadoInscripcion.ACEPTADO &&
            documentosAprobados == TipoDocumento.entries.count(TipoDocumento::obligatorio)
}

class AdminAspiranteDetailViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAspiranteDetailUiState())
    val uiState: StateFlow<AdminAspiranteDetailUiState> = _uiState.asStateFlow()

    fun cargarDetalle(aspiranteId: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            actualizarDetalle(aspiranteId)
        }
    }

    fun onObservacionesChange(v: String) = _uiState.update { it.copy(observacionesInput = v) }

    fun validarDocumento(docId: String, aprobado: Boolean) {
        val estado = _uiState.value
        val aspirante = estado.aspirante ?: return
        if (estado.isLoading || estado.documentos.none { it.id == docId && it.estado == EstadoDocumento.SUBIDO }) return

        val observaciones = estado.observacionesInput.trim()
        if (!aprobado && observaciones.isBlank()) {
            _uiState.update { it.copy(mensajeExito = null, mensajeError = "Ingrese el motivo del rechazo") }
            return
        }

        _uiState.update { it.copy(isLoading = true, mensajeExito = null, mensajeError = null) }
        viewModelScope.launch {
            repository.revisarDocumento(docId, aprobado, observaciones)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            observacionesInput = "",
                            mensajeExito = if (aprobado) "Documento aprobado" else "Documento rechazado"
                        )
                    }
                    actualizarDetalle(aspirante.id)
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo guardar el dictamen")
                    }
                }
        }
    }

    fun aprobarAspirante() {
        val estado = _uiState.value
        val aspirante = estado.aspirante ?: return
        if (estado.isLoading || !estado.puedeAprobar) return
        _uiState.update { it.copy(isLoading = true, mensajeExito = null, mensajeError = null) }
        viewModelScope.launch {
            repository.aprobarAspirante(aspirante.id)
                .onSuccess { actualizado ->
                    _uiState.update {
                        it.copy(aspirante = actualizado, isLoading = false, mensajeExito = "Trámite aprobado")
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo aprobar el trámite")
                    }
                }
        }
    }

    fun descargarPdf(documentoId: String, nombreArchivo: String?) {
        if (_uiState.value.isDownloadingPdf) return
        _uiState.update { it.copy(isDownloadingPdf = true, mensajeError = null) }
        viewModelScope.launch {
            repository.descargarDocumentoParaPrevisualizacion(documentoId, nombreArchivo)
                .onSuccess { file ->
                    _uiState.update { it.copy(isDownloadingPdf = false, pdfPreviewFile = file) }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isDownloadingPdf = false, mensajeError = ex.message ?: "No se pudo descargar el PDF")
                    }
                }
        }
    }

    fun cerrarPdfPreview() {
        _uiState.update { it.copy(pdfPreviewFile = null) }
    }

    private suspend fun actualizarDetalle(aspiranteId: String) {
        resultadoSolicitud {
            repository.getAspirantePorId(aspiranteId) to repository.getDocumentosAspirante(aspiranteId)
        }.onSuccess { (aspirante, documentos) ->
            _uiState.update { it.copy(aspirante = aspirante, documentos = documentos, isLoading = false) }
        }.onFailure { ex ->
            _uiState.update {
                it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cargar el expediente")
            }
        }
    }
}
