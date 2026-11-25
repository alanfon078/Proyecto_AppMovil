package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class AdminFechasUiState(
    val fechas: List<FechaImportante> = emptyList(),
    val isLoading: Boolean = false,
    val showDialogNuevaFecha: Boolean = false,
    val nuevoTitulo: String = "",
    val nuevaDescripcion: String = "",
    val nuevaFechaLimite: String = "",
    val mensajeExito: String? = null,
    val mensajeError: String? = null
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
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            actualizarFechas()
        }
    }

    fun abrirDialogoNuevaFecha() {
        if (_uiState.value.isLoading) return
        _uiState.update {
            it.copy(
                showDialogNuevaFecha = true,
                nuevoTitulo = "",
                nuevaDescripcion = "",
                nuevaFechaLimite = "",
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun cerrarDialogoNuevaFecha() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(showDialogNuevaFecha = false, mensajeError = null) }
    }

    fun onTituloChange(v: String) = _uiState.update { it.copy(nuevoTitulo = v) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(nuevaDescripcion = v) }
    fun onFechaLimiteChange(v: String) = _uiState.update { it.copy(nuevaFechaLimite = v) }

    fun guardarNuevaFecha() {
        val estado = _uiState.value
        if (estado.isLoading) return
        val titulo = estado.nuevoTitulo.trim()
        if (titulo.isEmpty()) {
            _uiState.update { it.copy(mensajeError = "Ingrese el título de la fecha límite") }
            return
        }
        val fecha = try {
            LocalDate.parse(estado.nuevaFechaLimite.trim()).toString()
        } catch (_: DateTimeParseException) {
            _uiState.update { it.copy(mensajeError = "Ingrese una fecha válida con formato AAAA-MM-DD") }
            return
        }

        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            repository.crearFechaImportante(titulo, estado.nuevaDescripcion.trim(), fecha)
                .onSuccess {
                    _uiState.update {
                        it.copy(showDialogNuevaFecha = false, mensajeExito = "Fecha límite programada")
                    }
                    actualizarFechas()
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo programar la fecha límite")
                    }
                }
        }
    }

    private suspend fun actualizarFechas() {
        resultadoSolicitud { repository.getFechasImportantes() }
            .onSuccess { fechas ->
                _uiState.update { it.copy(fechas = fechas, isLoading = false) }
            }.onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudieron cargar las fechas") }
            }
    }
}
