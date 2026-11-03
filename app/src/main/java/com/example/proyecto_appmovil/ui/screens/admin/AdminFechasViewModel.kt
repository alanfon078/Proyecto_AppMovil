package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.FechaImportante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminFechasUiState(
    val fechas: List<FechaImportante> = emptyList(),
    val isLoading: Boolean = false,
    val showDialogNuevaFecha: Boolean = false,
    val nuevoTitulo: String = "",
    val nuevaDescripcion: String = "",
    val nuevaFechaLimite: String = "",
    val nuevosDiasRestantes: String = "",
    val mensajeExito: String? = null
)

class AdminFechasViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminFechasUiState())
    val uiState: StateFlow<AdminFechasUiState> = _uiState.asStateFlow()

    init {
        cargarFechas()
    }

    fun cargarFechas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val list = repository.getFechasImportantes()
            _uiState.update { it.copy(fechas = list, isLoading = false) }
        }
    }

    fun abrirDialogoNuevaFecha() {
        _uiState.update {
            it.copy(
                showDialogNuevaFecha = true,
                nuevoTitulo = "",
                nuevaDescripcion = "",
                nuevaFechaLimite = "",
                nuevosDiasRestantes = "",
                mensajeExito = null
            )
        }
    }

    fun cerrarDialogoNuevaFecha() {
        _uiState.update { it.copy(showDialogNuevaFecha = false) }
    }

    fun onTituloChange(v: String) = _uiState.update { it.copy(nuevoTitulo = v) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(nuevaDescripcion = v) }
    fun onFechaLimiteChange(v: String) = _uiState.update { it.copy(nuevaFechaLimite = v) }
    fun onDiasRestantesChange(v: String) = _uiState.update { it.copy(nuevosDiasRestantes = v) }

    fun guardarNuevaFecha() {
        val s = _uiState.value
        if (s.nuevoTitulo.isBlank() || s.nuevaFechaLimite.isBlank()) return

        val dias = s.nuevosDiasRestantes.toIntOrNull() ?: 15
        val nueva = FechaImportante(
            id = "fecha_${System.currentTimeMillis()}",
            titulo = s.nuevoTitulo,
            descripcion = s.nuevaDescripcion,
            fechaLimite = s.nuevaFechaLimite,
            diasRestantes = dias
        )

        _uiState.update {
            it.copy(
                fechas = it.fechas + nueva,
                showDialogNuevaFecha = false,
                mensajeExito = "Nueva fecha límite programada correctamente."
            )
        }
    }
}
