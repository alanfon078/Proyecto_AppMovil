package com.example.proyecto_appmovil.ui.screens.questions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.CategoriaPregunta
import com.example.proyecto_appmovil.model.PreguntaDuda
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StudentQuestionsUiState(
    val categoria: CategoriaPregunta = CategoriaPregunta.FECHAS,
    val texto: String = "",
    val preguntas: List<PreguntaDuda> = emptyList(),
    val isLoading: Boolean = false,
    val mensajeError: String? = null,
    val mensajeExito: String? = null
)

class StudentQuestionsViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StudentQuestionsUiState())
    val uiState: StateFlow<StudentQuestionsUiState> = _uiState.asStateFlow()

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            try {
                val preguntas = repository.getMisPreguntas()
                _uiState.update { it.copy(preguntas = preguntas, isLoading = false) }
            } catch (ex: Exception) {
                _uiState.update { it.copy(isLoading = false, mensajeError = ex.message) }
            }
        }
    }

    fun onCategoriaChange(categoria: CategoriaPregunta) =
        _uiState.update { it.copy(categoria = categoria, mensajeError = null) }

    fun onTextoChange(texto: String) =
        _uiState.update { it.copy(texto = texto, mensajeError = null, mensajeExito = null) }

    fun enviar() {
        val state = _uiState.value
        val texto = state.texto.trim()
        if (texto.isBlank()) {
            _uiState.update { it.copy(mensajeError = "Escribe tu pregunta antes de enviarla") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            repository.crearPregunta(state.categoria.valorApi, texto)
                .onSuccess {
                    _uiState.update {
                        it.copy(texto = "", isLoading = false, mensajeExito = "Pregunta enviada")
                    }
                    cargar()
                }.onFailure { ex ->
                    _uiState.update { it.copy(isLoading = false, mensajeError = ex.message) }
                }
        }
    }
}
