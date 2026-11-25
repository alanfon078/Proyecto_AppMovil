package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminBroadcastUiState(
    val titulo: String = "",
    val mensaje: String = "",
    val showConfirmacion: Boolean = false,
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class AdminBroadcastViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminBroadcastUiState())
    val uiState: StateFlow<AdminBroadcastUiState> = _uiState.asStateFlow()

    fun onTituloChange(value: String) =
        _uiState.update { it.copy(titulo = value, mensajeError = null, mensajeExito = null) }

    fun onMensajeChange(value: String) =
        _uiState.update { it.copy(mensaje = value, mensajeError = null, mensajeExito = null) }

    fun solicitarEnvio() {
        val state = _uiState.value
        if (state.titulo.trim().isBlank()) {
            _uiState.update { it.copy(mensajeError = "Indica el título del aviso") }
            return
        }
        if (state.mensaje.trim().isBlank()) {
            _uiState.update { it.copy(mensajeError = "Escribe el mensaje del aviso") }
            return
        }
        _uiState.update { it.copy(showConfirmacion = true) }
    }

    fun cancelarEnvio() = _uiState.update { it.copy(showConfirmacion = false) }

    fun enviar() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, showConfirmacion = false) }
            repository.enviarAvisoGeneral(state.titulo.trim(), state.mensaje.trim())
                .onSuccess { count ->
                    _uiState.update {
                        it.copy(
                            titulo = "",
                            mensaje = "",
                            isLoading = false,
                            mensajeExito = "Aviso enviado a $count aspirantes"
                        )
                    }
                }.onFailure { ex ->
                    _uiState.update { it.copy(isLoading = false, mensajeError = ex.message) }
                }
        }
    }
}
