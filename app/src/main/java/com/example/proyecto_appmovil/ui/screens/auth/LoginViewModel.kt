package com.example.proyecto_appmovil.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val usuario: String = "",
    val contrasena: String = "",
    val errorUsuario: String? = null,
    val errorContrasena: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val loginSuccessRol: String? = null // "aspirante" o "escolar"
)

class LoginViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onUsuarioChange(nuevo: String) {
        _uiState.update { it.copy(usuario = nuevo, errorUsuario = null, errorMessage = null) }
    }

    fun onContrasenaChange(nuevo: String) {
        _uiState.update { it.copy(contrasena = nuevo, errorContrasena = null, errorMessage = null) }
    }

    fun setDemoAspirante() {
        _uiState.update {
            it.copy(
                usuario = "aspirante@itsur.edu.mx",
                contrasena = "123456",
                errorUsuario = null,
                errorContrasena = null
            )
        }
    }

    fun setDemoEscolar() {
        _uiState.update {
            it.copy(
                usuario = "escolar@itsur.edu.mx",
                contrasena = "admin123",
                errorUsuario = null,
                errorContrasena = null
            )
        }
    }

    fun login(onSuccess: (String) -> Unit) {
        val user = _uiState.value.usuario.trim()
        val pass = _uiState.value.contrasena.trim()

        var hasError = false
        if (user.isBlank()) {
            _uiState.update { it.copy(errorUsuario = "Ingrese su usuario o correo") }
            hasError = true
        }
        if (pass.isBlank()) {
            _uiState.update { it.copy(errorContrasena = "Ingrese su contraseña") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = repository.login(user, pass)
            result.onSuccess { aspirante ->
                _uiState.update { it.copy(isLoading = false, loginSuccessRol = aspirante.rol) }
                onSuccess(aspirante.rol)
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Credenciales inválidas"
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }
}
