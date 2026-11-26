package com.example.proyecto_appmovil.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyecto_appmovil.data.local.entity.CarreraEntity
import com.example.proyecto_appmovil.data.repository.InscripcionesRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

data class RegisterUiState(
    val nombres: String = "",
    val apellidos: String = "",
    val nombreUsuario: String = "",
    val curp: String = "",
    val email: String = "",
    val telefono: String = "",
    val idCarrera: Int? = null,
    val carreras: List<CarreraEntity> = emptyList(),
    val cargandoCarreras: Boolean = false,
    val errorCatalogo: String? = null,
    val calle: String = "",
    val estado: String = "",
    val municipio: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val errorNombres: String? = null,
    val errorApellidos: String? = null,
    val errorNombreUsuario: String? = null,
    val verificandoUsuario: Boolean = false,
    val usuarioDisponible: Boolean? = null,
    val errorCurp: String? = null,
    val errorEmail: String? = null,
    val errorTelefono: String? = null,
    val errorCarrera: String? = null,
    val errorCalle: String? = null,
    val errorEstado: String? = null,
    val errorMunicipio: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmarContrasena: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)

class RegisterViewModel(
    private val repository: InscripcionesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()
    private var checkUsernameJob: Job? = null
    private val usernameRegex = Regex("^[a-z0-9_.-]{3,50}$")

    init {
        viewModelScope.launch {
            repository.getCarrerasFlow().collect { carreras ->
                _uiState.update { it.copy(carreras = carreras.filter(CarreraEntity::activa)) }
            }
        }
        cargarCarreras()
    }

    fun cargarCarreras() {
        if (_uiState.value.cargandoCarreras) return
        _uiState.update { it.copy(cargandoCarreras = true, errorCatalogo = null) }
        viewModelScope.launch {
            val resultado = repository.refreshCatalogos()
            _uiState.update {
                it.copy(
                    cargandoCarreras = false,
                    errorCatalogo = resultado.exceptionOrNull()?.message
                )
            }
        }
    }

    fun onNombresChange(v: String) = _uiState.update { it.copy(nombres = v, errorNombres = null) }
    fun onApellidosChange(v: String) = _uiState.update { it.copy(apellidos = v, errorApellidos = null) }
    fun onNombreUsuarioChange(v: String) {
        val sanitized = v.filterNot { it.isWhitespace() }.take(50)
        val normalized = sanitized.lowercase(Locale.ROOT)
        checkUsernameJob?.cancel()
        _uiState.update {
            it.copy(
                nombreUsuario = sanitized,
                errorNombreUsuario = null,
                usuarioDisponible = null,
                verificandoUsuario = false
            )
        }
        if (normalized.length >= 3 && usernameRegex.matches(normalized)) {
            _uiState.update { it.copy(verificandoUsuario = true) }
            checkUsernameJob = viewModelScope.launch {
                delay(400)
                repository.verificarDisponibilidadUsuario(normalized)
                    .onSuccess { disponible ->
                        _uiState.update {
                            if (it.nombreUsuario.lowercase(Locale.ROOT) != normalized) it
                            else it.copy(
                                verificandoUsuario = false,
                                usuarioDisponible = disponible,
                                errorNombreUsuario = if (disponible) null else "Este nombre de usuario ya está en uso"
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(verificandoUsuario = false) }
                    }
            }
        }
    }
    fun onCurpChange(v: String) = _uiState.update { it.copy(curp = v.uppercase().take(18), errorCurp = null) }
    fun onEmailChange(v: String) = _uiState.update { it.copy(email = v, errorEmail = null) }
    fun onTelefonoChange(v: String) = _uiState.update { it.copy(telefono = v.filter { ch -> ch.isDigit() }.take(10), errorTelefono = null) }
    fun onCarreraChange(id: Int) = _uiState.update { it.copy(idCarrera = id, errorCarrera = null) }
    fun onCalleChange(v: String) = _uiState.update { it.copy(calle = v.take(255), errorCalle = null) }
    fun onEstadoChange(v: String) = _uiState.update { it.copy(estado = v, errorEstado = null) }
    fun onMunicipioChange(v: String) = _uiState.update { it.copy(municipio = v.take(120), errorMunicipio = null) }
    fun onContrasenaChange(v: String) = _uiState.update { it.copy(contrasena = v, errorContrasena = null) }
    fun onConfirmarContrasenaChange(v: String) = _uiState.update { it.copy(confirmarContrasena = v, errorConfirmarContrasena = null) }

    fun registrar(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.isLoading) return
        val idCarrera = s.idCarrera
        var valid = true

        if (s.nombres.isBlank()) {
            _uiState.update { it.copy(errorNombres = "Ingrese su(s) nombre(s)") }
            valid = false
        }
        if (s.apellidos.isBlank()) {
            _uiState.update { it.copy(errorApellidos = "Ingrese sus apellidos") }
            valid = false
        }
        val normalizedUsername = s.nombreUsuario.trim().lowercase(Locale.ROOT)
        if (normalizedUsername.isBlank()) {
            _uiState.update { it.copy(errorNombreUsuario = "Ingrese un nombre de usuario") }
            valid = false
        } else if (!usernameRegex.matches(normalizedUsername)) {
            _uiState.update {
                it.copy(errorNombreUsuario = "Debe tener 3 a 50 caracteres (letras, números, ., _, -)")
            }
            valid = false
        } else if (s.usuarioDisponible == false) {
            _uiState.update { it.copy(errorNombreUsuario = "Este nombre de usuario ya está en uso") }
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
        if (idCarrera == null || s.carreras.none { it.idCarrera == idCarrera }) {
            _uiState.update { it.copy(errorCarrera = "Seleccione una carrera") }
            valid = false
        }
        if (s.calle.isBlank()) {
            _uiState.update { it.copy(errorCalle = "Ingrese su calle") }
            valid = false
        }
        if (s.estado !in estadosDeMexico) {
            _uiState.update { it.copy(errorEstado = "Seleccione su estado de residencia") }
            valid = false
        }
        if (s.municipio.isBlank()) {
            _uiState.update { it.copy(errorMunicipio = "Ingrese su municipio de residencia") }
            valid = false
        }
        if (Character.codePointCount(s.contrasena, 0, s.contrasena.length) < 8) {
            _uiState.update { it.copy(errorContrasena = "La contraseña debe tener mínimo 8 caracteres") }
            valid = false
        }
        if (s.contrasena != s.confirmarContrasena) {
            _uiState.update { it.copy(errorConfirmarContrasena = "Las contraseñas no coinciden") }
            valid = false
        }

        if (!valid || idCarrera == null) return

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val nombreCompleto = "${s.nombres.trim()} ${s.apellidos.trim()}"
            val result = repository.registrarAspirante(
                nombreCompleto = nombreCompleto,
                nombreUsuario = normalizedUsername,
                curp = s.curp,
                email = s.email.trim(),
                telefono = s.telefono,
                idCarrera = idCarrera,
                direccion = s.calle.trim(),
                estadoResidencia = s.estado,
                municipioResidencia = s.municipio.trim(),
                contrasena = s.contrasena
            )

            result.onSuccess {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            }.onFailure { ex ->
                val msg = ex.message ?: "Error al registrar aspirante"
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = msg,
                        errorNombreUsuario = if (msg.contains("usuario", ignoreCase = true)) msg else it.errorNombreUsuario
                    )
                }
            }
        }
    }
}
