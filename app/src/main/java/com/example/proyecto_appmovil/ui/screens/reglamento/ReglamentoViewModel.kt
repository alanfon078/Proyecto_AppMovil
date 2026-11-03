package com.example.proyecto_appmovil.ui.screens.reglamento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReglamentoUiState(
    val aceptoTerminos: Boolean = false,
    val tieneFirma: Boolean = false,
    val isFirmadoExitosamente: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class ReglamentoViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReglamentoUiState())
    val uiState: StateFlow<ReglamentoUiState> = _uiState.asStateFlow()

    init {
        verificarFirma()
    }

    private fun verificarFirma() {
        viewModelScope.launch {
            val firmado = repository.isReglamentoFirmado()
            _uiState.update { it.copy(isFirmadoExitosamente = firmado) }
        }
    }

    fun toggleTerminos(v: Boolean) = _uiState.update { it.copy(aceptoTerminos = v, errorMessage = null) }
    fun setTieneFirma(v: Boolean) = _uiState.update { it.copy(tieneFirma = v, errorMessage = null) }

    fun guardarFirma() {
        val s = _uiState.value
        if (!s.aceptoTerminos) {
            _uiState.update { it.copy(errorMessage = "Debes marcar la casilla aceptando los términos y el reglamento") }
            return
        }
        if (!s.tieneFirma) {
            _uiState.update { it.copy(errorMessage = "Por favor dibuja tu firma en el recuadro") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.firmarReglamento("firma_capturada")
            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isFirmadoExitosamente = true) }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, errorMessage = ex.message ?: "Error al guardar firma") }
            }
        }
    }
}
