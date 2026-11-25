package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.PreguntaDuda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminQuestionsUiState(
    val categoria: String? = null,
    val estado: String? = null,
    val preguntas: List<PreguntaDuda> = emptyList(),
    val preguntaSeleccionada: PreguntaDuda? = null,
    val respuesta: String = "",
    val showRespuesta: Boolean = false,
    val isLoading: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class AdminQuestionsViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminQuestionsUiState())
    val uiState: StateFlow<AdminQuestionsUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun setCategoria(categoria: String?) {
        _uiState.update { it.copy(categoria = categoria) }
        cargar()
    }

    fun setEstado(estado: String?) {
        _uiState.update { it.copy(estado = estado) }
        cargar()
    }

    fun cargar() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            try {
                val preguntas = repository.getPreguntasAdmin(state.categoria, state.estado)
                _uiState.update { it.copy(preguntas = preguntas, isLoading = false) }
            } catch (ex: Exception) {
                _uiState.update { it.copy(isLoading = false, mensajeError = ex.message) }
            }
        }
    }

    fun abrirRespuesta(pregunta: PreguntaDuda) =
        _uiState.update {
            it.copy(preguntaSeleccionada = pregunta, respuesta = pregunta.respuesta.orEmpty(), showRespuesta = true)
        }

    fun cerrarRespuesta() =
        _uiState.update { it.copy(showRespuesta = false, preguntaSeleccionada = null, respuesta = "") }

    fun onRespuestaChange(value: String) =
        _uiState.update { it.copy(respuesta = value, mensajeError = null) }

    fun responder() {
        val state = _uiState.value
        val pregunta = state.preguntaSeleccionada ?: return
        val respuesta = state.respuesta.trim()
        if (respuesta.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Escribe una respuesta") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.responderPregunta(pregunta.id, respuesta)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            showRespuesta = false,
                            preguntaSeleccionada = null,
                            respuesta = "",
                            mensajeExito = "Respuesta enviada",
                            isLoading = false
                        )
                    }
                    cargar()
                }.onFailure { ex ->
                    _uiState.update { it.copy(isLoading = false, mensajeError = ex.message) }
                }
        }
    }
}
