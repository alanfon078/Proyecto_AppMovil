package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.TipoFechaImportante
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
    val fechaEnEdicionId: String? = null,
    val nuevoTitulo: String = "",
    val nuevaDescripcion: String = "",
    val nuevaFechaLimite: String = "",
    val nuevoTipo: TipoFechaImportante = TipoFechaImportante.LIMITE_ENTREGA,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class AdminFechasViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminFechasUiState())
    val uiState: StateFlow<AdminFechasUiState> = _uiState.asStateFlow()

    init { cargarFechas() }

    fun cargarFechas() {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch { actualizarFechas() }
    }

    fun abrirDialogoNuevaFecha() {
        _uiState.update {
            it.copy(
                showDialogNuevaFecha = true,
                fechaEnEdicionId = null,
                nuevoTitulo = "",
                nuevaDescripcion = "",
                nuevaFechaLimite = "",
                nuevoTipo = TipoFechaImportante.LIMITE_ENTREGA,
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun abrirDialogoEditar(fecha: FechaImportante) {
        _uiState.update {
            it.copy(
                showDialogNuevaFecha = true,
                fechaEnEdicionId = fecha.id,
                nuevoTitulo = fecha.titulo,
                nuevaDescripcion = fecha.descripcion,
                nuevaFechaLimite = fecha.fechaLimite.take(10),
                nuevoTipo = TipoFechaImportante.fromApi(fecha.tipo),
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun cerrarDialogoNuevaFecha() {
        _uiState.update { it.copy(showDialogNuevaFecha = false, fechaEnEdicionId = null, mensajeError = null) }
    }

    fun onTituloChange(v: String) = _uiState.update { it.copy(nuevoTitulo = v) }
    fun onDescripcionChange(v: String) = _uiState.update { it.copy(nuevaDescripcion = v) }
    fun onFechaLimiteChange(v: String) = _uiState.update { it.copy(nuevaFechaLimite = v) }
    fun onTipoChange(tipo: TipoFechaImportante) = _uiState.update { it.copy(nuevoTipo = tipo) }

    fun guardarFecha() {
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
            _uiState.update { it.copy(mensajeError = "Seleccione una fecha válida") }
            return
        }

        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            val resultado = estado.fechaEnEdicionId?.let { id ->
                repository.actualizarFechaImportante(
                    id, titulo, estado.nuevaDescripcion.trim(),
                    estado.nuevoTipo.valorApi, fecha
                )
            } ?: repository.crearFechaImportante(
                titulo, estado.nuevaDescripcion.trim(), estado.nuevoTipo.valorApi, fecha
            )
            resultado.onSuccess {
                _uiState.update {
                    it.copy(
                        showDialogNuevaFecha = false,
                        fechaEnEdicionId = null,
                        mensajeExito = "Fecha límite guardada"
                    )
                }
                actualizarFechas()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo guardar la fecha límite")
                }
            }
        }
    }

    fun eliminarFecha(id: String) {
        if (_uiState.value.isLoading) return
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        viewModelScope.launch {
            repository.eliminarFechaImportante(id)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false, mensajeExito = "Fecha eliminada") }
                    actualizarFechas()
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo eliminar la fecha")
                    }
                }
        }
    }

    private suspend fun actualizarFechas() {
        resultadoSolicitud { repository.getFechasImportantes() }
            .onSuccess { fechas -> _uiState.update { it.copy(fechas = fechas, isLoading = false) } }
            .onFailure { ex ->
                _uiState.update { it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudieron cargar las fechas") }
            }
    }
}
