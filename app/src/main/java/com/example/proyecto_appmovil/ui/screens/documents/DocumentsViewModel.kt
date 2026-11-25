package com.example.proyecto_appmovil.ui.screens.documents

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConfirmacionEnvioPdf(
    val tipo: TipoDocumento,
    val uri: Uri,
    val nombreArchivo: String,
    val tamanoBytes: Long,
    val esReemplazo: Boolean
)

data class DocumentsUiState(
    val documentos: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val documentoSeleccionadoParaSubir: TipoDocumento? = null,
    val confirmacionEnvio: ConfirmacionEnvioPdf? = null,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class DocumentsViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState.asStateFlow()

    init {
        cargarDocumentos()
    }

    fun cargarDocumentos() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            actualizarDocumentos()
        }
    }

    fun prepararSubida(tipo: TipoDocumento) {
        _uiState.update { it.copy(documentoSeleccionadoParaSubir = tipo, mensajeExito = null, mensajeError = null) }
    }

    fun onArchivoSeleccionado(uri: Uri?) {
        val estado = _uiState.value
        val tipo = estado.documentoSeleccionadoParaSubir ?: return
        _uiState.update { it.copy(documentoSeleccionadoParaSubir = null) }
        if (uri == null || estado.isLoading) return

        viewModelScope.launch {
            repository.obtenerMetadataPdf(uri)
                .onSuccess { meta ->
                    val yaSubido = estado.documentos.any { it.tipo == tipo && it.nombreArchivo != null }
                    _uiState.update {
                        it.copy(
                            confirmacionEnvio = ConfirmacionEnvioPdf(
                                tipo = tipo,
                                uri = uri,
                                nombreArchivo = meta.originalName,
                                tamanoBytes = meta.sizeBytes,
                                esReemplazo = yaSubido
                            ),
                            mensajeError = null
                        )
                    }
                }
                .onFailure { ex ->
                    _uiState.update {
                        it.copy(mensajeError = ex.message ?: "El archivo seleccionado no es un PDF válido")
                    }
                }
        }
    }

    fun cancelarEnvioDocumento() {
        _uiState.update { it.copy(confirmacionEnvio = null) }
    }

    fun confirmarEnvioDocumento() {
        val estado = _uiState.value
        val pending = estado.confirmacionEnvio ?: return
        if (estado.isLoading) return

        _uiState.update {
            it.copy(
                confirmacionEnvio = null,
                isLoading = true,
                mensajeError = null,
                mensajeExito = null
            )
        }
        viewModelScope.launch {
            repository.subirDocumento(pending.tipo, pending.uri)
                .onSuccess {
                    _uiState.update { it.copy(mensajeExito = "${pending.tipo.titulo} enviado a revisión") }
                    actualizarDocumentos()
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo subir el archivo")
                    }
                }
        }
    }

    private suspend fun actualizarDocumentos() {
        resultadoSolicitud { repository.getDocumentos() }
            .onSuccess { docs ->
                _uiState.update { it.copy(documentos = docs, isLoading = false) }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudieron cargar los documentos")
                }
            }
    }
}
