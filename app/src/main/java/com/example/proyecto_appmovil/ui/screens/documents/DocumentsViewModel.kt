package com.example.proyecto_appmovil.ui.screens.documents

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.TipoDocumento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DocumentsUiState(
    val documentos: List<Documento> = emptyList(),
    val isLoading: Boolean = false,
    val documentoSeleccionadoParaSubir: TipoDocumento? = null,
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val docs = repository.getDocumentos()
            _uiState.update { it.copy(documentos = docs, isLoading = false) }
        }
    }

    fun prepararSubida(tipo: TipoDocumento) {
        _uiState.update { it.copy(documentoSeleccionadoParaSubir = tipo, mensajeExito = null, mensajeError = null) }
    }

    fun onArchivoSeleccionado(uriString: String, nombre: String, tamano: Long) {
        val tipo = _uiState.value.documentoSeleccionadoParaSubir ?: return

        // Validación de tamaño (máximo 5MB)
        if (tamano > 5 * 1024 * 1024) {
            _uiState.update {
                it.copy(mensajeError = "El archivo excede el tamaño máximo permitido de 5 MB")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            val result = repository.subirDocumento(
                tipo = tipo,
                nombreArchivo = nombre,
                uriString = uriString,
                tamanoBytes = tamano
            )
            result.onSuccess {
                val updatedDocs = repository.getDocumentos()
                _uiState.update {
                    it.copy(
                        documentos = updatedDocs,
                        isLoading = false,
                        mensajeExito = "¡${tipo.titulo} subido exitosamente a revisión!",
                        documentoSeleccionadoParaSubir = null
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensajeError = ex.message ?: "Error al subir el archivo"
                    )
                }
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(mensajeExito = null, mensajeError = null) }
    }
}
