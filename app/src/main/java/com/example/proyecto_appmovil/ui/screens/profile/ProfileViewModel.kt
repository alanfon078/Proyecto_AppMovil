package com.example.proyecto_appmovil.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import com.example.proyecto_appmovil.model.Aspirante
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
    val carreraEdit: String = "",
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
            val asp = repository.getAspiranteActual()
            _uiState.update {
                it.copy(
                    aspirante = asp,
                    telefonoEdit = asp.telefono,
                    direccionEdit = asp.direccion,
                    carreraEdit = asp.carrera,
                    isLoading = false
                )
            }
        }
    }

    fun toggleEditMode() {
        val current = _uiState.value
        _uiState.update {
            it.copy(
                isEditMode = !current.isEditMode,
                telefonoEdit = current.aspirante?.telefono ?: "",
                direccionEdit = current.aspirante?.direccion ?: "",
                carreraEdit = current.aspirante?.carrera ?: "",
                mensajeExito = null,
                mensajeError = null
            )
        }
    }

    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefonoEdit = v.filter { ch -> ch.isDigit() }.take(10)) }
    fun onDireccionChange(v: String) = _uiState.update { it.copy(direccionEdit = v) }
    fun onCarreraChange(v: String) = _uiState.update { it.copy(carreraEdit = v) }
    fun onPasswordActualChange(v: String) = _uiState.update { it.copy(passwordActual = v) }
    fun onPasswordNuevaChange(v: String) = _uiState.update { it.copy(passwordNueva = v) }

    fun guardarCambios() {
        val s = _uiState.value
        val asp = s.aspirante ?: return

        if (s.telefonoEdit.length != 10) {
            _uiState.update { it.copy(mensajeError = "El teléfono debe contener 10 dígitos") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, mensajeError = null) }
            val updated = asp.copy(
                telefono = s.telefonoEdit,
                direccion = s.direccionEdit,
                carrera = s.carreraEdit
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
        if (s.passwordNueva.length < 6) {
            _uiState.update { it.copy(mensajeError = "La nueva contraseña debe tener mínimo 6 caracteres") }
            return
        }
        _uiState.update {
            it.copy(
                passwordActual = "",
                passwordNueva = "",
                mensajeExito = "Contraseña cambiada con éxito"
            )
        }
    }
}
