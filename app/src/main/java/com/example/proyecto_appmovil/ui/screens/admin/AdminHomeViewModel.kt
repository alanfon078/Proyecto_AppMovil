package com.example.proyecto_appmovil.ui.screens.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

data class AdminHomeUiState(
    val aspirantes: List<Aspirante> = emptyList(),
    val filtroBusqueda: String = "",
    val estadoFiltroSeleccionado: EstadoInscripcion? = null,
    val totalAspirantes: Int = 0,
    val inRevision: Int = 0,
    val aprobados: Int = 0,
    val enCaptura: Int = 0,
    val conObservaciones: Int = 0,
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class AdminHomeViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()
    private var carga: Job? = null

    fun cargarAspirantes() {
        consultarAspirantes(esperaMillis = 0)
    }

    fun onBusquedaChange(query: String) {
        _uiState.update { it.copy(filtroBusqueda = query, aspirantes = emptyList()) }
        consultarAspirantes(esperaMillis = 250)
    }

    fun onFiltroEstadoChange(estado: EstadoInscripcion?) {
        _uiState.update { it.copy(estadoFiltroSeleccionado = estado, aspirantes = emptyList()) }
        cargarAspirantes()
    }

    private fun consultarAspirantes(esperaMillis: Long) {
        carga?.cancel()
        val filtros = _uiState.value
        _uiState.update { it.copy(isLoading = true, mensajeError = null) }
        carga = viewModelScope.launch {
            delay(esperaMillis)
            actualizarAspirantes(filtros)
        }
    }

    private suspend fun actualizarAspirantes(filtros: AdminHomeUiState) {
        val estado = filtros.estadoFiltroSeleccionado
        val estadoApi = estado?.let {
            when (it) {
                EstadoInscripcion.PENDIENTE -> "en_captura"
                EstadoInscripcion.EN_REVISION -> "en_revision"
                EstadoInscripcion.CON_OBSERVACIONES -> "con_observaciones"
                EstadoInscripcion.ACEPTADO -> "documentacion_aprobada"
                EstadoInscripcion.RECHAZADO -> "rechazado"
            }
        }
        val query = filtros.filtroBusqueda.trim().ifBlank { null }
        val resultado = resultadoSolicitud {
            val todos = repository.getAspirantesParaRevision()
            val filtrados = if (estadoApi == null && query == null) todos
            else repository.getAspirantesParaRevision(estadoApi, query)
            todos to filtrados
        }
        coroutineContext.ensureActive()
        resultado.onSuccess { (todos, filtrados) ->
            _uiState.update {
                it.copy(
                    aspirantes = filtrados,
                    totalAspirantes = todos.size,
                    inRevision = todos.count { asp -> asp.estadoInscripcion == EstadoInscripcion.EN_REVISION },
                    aprobados = todos.count { asp -> asp.estadoInscripcion == EstadoInscripcion.ACEPTADO },
                    enCaptura = todos.count { asp -> asp.estadoInscripcion == EstadoInscripcion.PENDIENTE },
                    conObservaciones = todos.count { asp -> asp.estadoInscripcion == EstadoInscripcion.CON_OBSERVACIONES },
                    isLoading = false
                )
            }
        }.onFailure { ex ->
            _uiState.update { it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cargar el panel") }
        }
    }
}
