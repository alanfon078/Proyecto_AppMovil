package com.example.proyecto_appmovil.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RegisterUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val curp: String = "",
    val email: String = "",
    val telefono: String = "",
    val carrera: String = "Ing. Sistemas Computacionales",
    val estado: String = "Guanajuato",
    val municipio: String = "Uriangato",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val errorNombres: String? = null,
    val errorApellidos: String? = null,
    val errorCurp: String? = null,
    val errorEmail: String? = null,
    val errorTelefono: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    val carrerasItsur = listOf(
        "Ing. Sistemas Computacionales",
        "Ing. Sistemas Automotrices",
        "Ing. Ambiental",
        "Lic. Gestión Empresarial",
        "Ing. Industrial",
        "Ing. Electrónica",
        "Ing. Semiconductores",
        "Lic. Gastronomía y Turismo"
    )

    val municipiosGto = listOf(
        "Uriangato",
        "Moroleón",
        "Yuriria",
        "Salvatierra",
        "Valle de Santiago",
        "Celaya",
        "Irapuato",
        "León"
    )

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onNombresChange(v: String) = _uiState.update { it.copy(nombres = v, errorNombres = null) }
    fun onApellidosChange(v: String) = _uiState.update { it.copy(apellidos = v, errorApellidos = null) }
    fun onCurpChange(v: String) = _uiState.update { it.copy(curp = v.uppercase().take(18), errorCurp = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorEmail = null) }
    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefono = v.filter { ch -> ch.isDigit() }.take(10), errorTelefono = null) }
    fun onCarreraChange(v: String) = _uiState.update { it.copy(carrera = v) }
    fun onMunicipioChange(v: String) = _uiState.update { it.copy(municipio = v) }
    fun onContrasenaChange(v: String) = _uiState.update { it.copy(contrasena = v, errorContrasena = null) }
    fun onConfirmarContrasenaChange(v: String) = _uiState.update { it.copy(confirmarContrasena = v, errorConfirmarContrasena = null) }

    fun registrar(onSuccess: () -> Unit) {
        val s = _uiState.value
        var valid = true

        if (s.nombres.isBlank()) {
            _uiState.update { it.copy(errorNombres = "Ingrese su(s) nombre(s)") }
            valid = false
        }
        if (s.apellidos.isBlank()) {
            _uiState.update { it.copy(errorApellidos = "Ingrese sus apellidos") }
            valid = false
        }
        val curpRegex = Regex("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]\\d$")
        if (s.curp.length != 18 || !curpRegex.matches(s.curp)) {
            _uiState.update { it.copy(errorCurp = "CURP inválida (debe contener 18 caracteres oficiales)") }
            valid = false
        }
        if (!s.email.contains("@") || !s.email.contains(".")) {
            _uiState.update { it.copy(errorEmail = "Correo electrónico no válido") }
            valid = false
        }
        if (s.telefono.length != 10) {
            _uiState.update { it.copy(errorTelefono = "El teléfono debe ser de 10 dígitos") }
            valid = false
        }
        if (s.contrasena.length < 6) {
            _uiState.update { it.copy(errorContrasena = "La contraseña debe tener mínimo 6 caracteres") }
            valid = false
        }
        if (s.contrasena != s.confirmarContrasena) {
            _uiState.update { it.copy(errorConfirmarContrasena = "Las contraseñas no coinciden") }
            valid = false
        }

        if (!valid) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val nombreCompleto = "${s.nombres.trim()} ${s.apellidos.trim()}"
            val direccion = "${s.municipio}, ${s.estado}"
            val result = repository.registrarAspirante(
                nombreCompleto = nombreCompleto,
                curp = s.curp,
                email = s.email,
                telefono = s.telefono,
                carrera = s.carrera,
                direccion = direccion,
                contrasena = s.contrasena
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.message ?: "Error al registrar aspirante"
                    )
                }
            }
        }
    }
}
