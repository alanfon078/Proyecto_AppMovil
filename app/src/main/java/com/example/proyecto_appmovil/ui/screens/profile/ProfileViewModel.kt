package com.example.proyecto_appmovil.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.ui.resultadoSolicitud
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val aspirante: Aspirante? = null,
    val isEditMode: Boolean = false,
    val telefonoEdit: String = "",
    val direccionEdit: String = "",
    val estadoEdit: String = "",
    val municipioEdit: String = "",
    val passwordActual: String = "",
    val passwordNueva: String = "",
    val isLoading: Boolean = false,
    val mensajeExito: String? = null,
    val mensajeError: String? = null
)

class ProfileViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        cargarPerfil()
    }

    fun cargarPerfil() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            resultadoSolicitud { repository.getAspiranteActual() }
                .onSuccess { asp ->
                    _uiState.update {
                        it.copy(
                            aspirante = asp,
                            telefonoEdit = asp.telefono,
                            direccionEdit = asp.direccion,
                            estadoEdit = asp.estadoResidencia,
                            municipioEdit = asp.municipioResidencia,
                            isLoading = false
                        )
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cargar el perfil")
                    }
                }
        }
    }

    fun toggleEditMode() {
        val current = _uiState.value
        val aspirante = current.aspirante ?: return
        if (current.isLoading) return
        _uiState.update {
            it.copy(
                isEditMode = !current.isEditMode,
                telefonoEdit = aspirante.telefono,
                direccionEdit = aspirante.direccion,
                estadoEdit = aspirante.estadoResidencia,
                municipioEdit = aspirante.municipioResidencia,
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefonoEdit = v.filter { ch -> ch.isDigit() }.take(10)) }
    fun onDireccionChange(v: String) = _uiState.update { it.copy(direccionEdit = v.take(255)) }
    fun onEstadoChange(v: String) = _uiState.update { it.copy(estadoEdit = v.take(100)) }
    fun onMunicipioChange(v: String) = _uiState.update { it.copy(municipioEdit = v.take(120)) }
    fun onPasswordActualChange(v: String) = _uiState.update { it.copy(passwordActual = v) }
    fun onPasswordNuevaChange(v: String) = _uiState.update { it.copy(passwordNueva = v) }

    fun guardarCambios() {
        val s = _uiState.value
        if (s.isLoading) return
        val asp = s.aspirante ?: return

        if (s.telefonoEdit.length != 10) {
            _uiState.update { it.copy(mensajeExito = null, mensajeError = "El teléfono debe contener 10 dígitos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeExito = null, mensajeError = null) }
            val updated = asp.copy(
                telefono = s.telefonoEdit,
                direccion = s.direccionEdit.trim(),
                estadoResidencia = s.estadoEdit.trim(),
                municipioResidencia = s.municipioEdit.trim()
            )
            val result = repository.actualizarAspirante(updated)
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        aspirante = updated,
                        isEditMode = false,
                        isLoading = false,
                        mensajeExito = "¡Datos personales actualizados correctamente!"
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        mensajeError = ex.message ?: "Error al actualizar"
                    )
                }
            }
        }
    }

    fun cambiarPassword() {
        val s = _uiState.value
        if (s.isLoading) return
        val error = when {
            s.passwordActual.isEmpty() -> "Ingrese la contraseña actual"
            Character.codePointCount(s.passwordNueva, 0, s.passwordNueva.length) < 8 -> "La nueva contraseña debe tener mínimo 8 caracteres"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(mensajeExito = null, mensajeError = error) }
            return
        }

        _uiState.update { it.copy(isLoading = true, mensajeExito = null, mensajeError = null) }
        viewModelScope.launch {
            repository.cambiarPassword(s.passwordActual, s.passwordNueva)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            passwordActual = "",
                            passwordNueva = "",
                            mensajeExito = "Contraseña cambiada con éxito"
                        )
                    }
                }.onFailure { ex ->
                    _uiState.update {
                        it.copy(isLoading = false, mensajeError = ex.message ?: "No se pudo cambiar la contraseña")
                    }
                }
        }
    }
}
