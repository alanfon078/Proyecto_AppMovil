package com.example.proyecto_appmovil.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTextField
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.PrimaryButton
import com.example.proyecto_appmovil.ui.components.itsurTextFieldColors
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    RegisterContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNombresChange = viewModel::onNombresChange,
        onApellidosChange = viewModel::onApellidosChange,
        onNombreUsuarioChange = viewModel::onNombreUsuarioChange,
        onCurpChange = viewModel::onCurpChange,
        onEmailChange = viewModel::onEmailChange,
        onTelefonoChange = viewModel::onTelefonoChange,
        onCarreraChange = viewModel::onCarreraChange,
        onRecargarCarreras = viewModel::cargarCarreras,
        onEstadoChange = viewModel::onEstadoChange,
        onMunicipioChange = viewModel::onMunicipioChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onConfirmarContrasenaChange = viewModel::onConfirmarContrasenaChange,
        onRegistrarClick = { viewModel.registrar(onRegisterSuccess) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onNavigateBack: () -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onNombreUsuarioChange: (String) -> Unit,
    onCurpChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onCarreraChange: (Int) -> Unit,
    onRecargarCarreras: () -> Unit,
    onEstadoChange: (String) -> Unit,
    onMunicipioChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onConfirmarContrasenaChange: (String) -> Unit,
    onRegistrarClick: () -> Unit
) {
    var carreraExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        topBar = {
            ItsurTopBar(
                title = "Registro de Aspirante",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Datos del Nuevo Ingreso",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = ItsurVinoOscuro,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Completa la información oficial para darte de alta en el sistema.",
                style = MaterialTheme.typography.bodyMedium,
                color = ItsurOnSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 20.dp)
            )

            if (uiState.errorMessage != null) {
                ErrorBanner(mensaje = uiState.errorMessage ?: "")
            }

            ItsurTextField(
                value = uiState.nombres,
                onValueChange = onNombresChange,
                label = "Nombre(s)",
                leadingIcon = Icons.Default.Person,
                errorMessage = uiState.errorNombres,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.apellidos,
                onValueChange = onApellidosChange,
                label = "Apellidos",
                leadingIcon = Icons.Default.PersonOutline,
                errorMessage = uiState.errorApellidos,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.nombreUsuario,
                onValueChange = onNombreUsuarioChange,
                label = "Nombre de usuario",
                leadingIcon = Icons.Default.AlternateEmail,
                errorMessage = uiState.errorNombreUsuario,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (uiState.verificandoUsuario) {
                Text(
                    text = "Verificando disponibilidad...",
                    style = MaterialTheme.typography.labelSmall,
                    color = ItsurOnSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 4.dp)
                )
            } else if (uiState.usuarioDisponible == true && uiState.errorNombreUsuario == null) {
                Text(
                    text = "Nombre de usuario disponible",
                    style = MaterialTheme.typography.labelSmall,
                    color = StatusAprobado,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.curp,
                onValueChange = onCurpChange,
                label = "CURP (18 caracteres)",
                leadingIcon = Icons.Default.Badge,
                errorMessage = uiState.errorCurp,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                label = "Correo Electrónico",
                leadingIcon = Icons.Default.Email,
                errorMessage = uiState.errorEmail,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.telefono,
                onValueChange = onTelefonoChange,
                label = "Teléfono de Contacto (10 dígitos)",
                leadingIcon = Icons.Default.Phone,
                errorMessage = uiState.errorTelefono,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Carrera ITSUR
            ExposedDropdownMenuBox(
                expanded = carreraExpanded,
                onExpandedChange = { if (uiState.carreras.isNotEmpty()) carreraExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.carreras.firstOrNull { it.idCarrera == uiState.idCarrera }?.nombre.orEmpty(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Carrera de Interés") },
                    isError = uiState.errorCarrera != null,
                    supportingText = { uiState.errorCarrera?.let { Text(it) } },
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null, tint = ItsurCiruela)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = carreraExpanded)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = itsurTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = carreraExpanded,
                    onDismissRequest = { carreraExpanded = false }
                ) {
                    uiState.carreras.forEach { carrera ->
                        DropdownMenuItem(
                            text = { Text(carrera.nombre) },
                            onClick = {
                                onCarreraChange(carrera.idCarrera)
                                carreraExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.cargandoCarreras) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            uiState.errorCatalogo?.let { error ->
                ErrorBanner(mensaje = error)
                TextButton(onClick = onRecargarCarreras) { Text("Reintentar catálogo de carreras", color = ItsurOnSurface) }
            }

            ItsurTextField(
                value = uiState.estado,
                onValueChange = onEstadoChange,
                label = "Estado de Residencia",
                leadingIcon = Icons.Default.LocationOn,
                errorMessage = uiState.errorEstado,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.municipio,
                onValueChange = onMunicipioChange,
                label = "Municipio de Residencia",
                leadingIcon = Icons.Default.LocationOn,
                errorMessage = uiState.errorMunicipio,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.contrasena,
                onValueChange = onContrasenaChange,
                label = "Contraseña (mínimo 8 caracteres)",
                leadingIcon = Icons.Default.Lock,
                isPassword = true,
                errorMessage = uiState.errorContrasena,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.confirmarContrasena,
                onValueChange = onConfirmarContrasenaChange,
                label = "Confirmar Contraseña",
                leadingIcon = Icons.Default.LockReset,
                isPassword = true,
                errorMessage = uiState.errorConfirmarContrasena,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onRegistrarClick() }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(28.dp))

            PrimaryButton(
                text = "Registrar Aspirante",
                isLoading = uiState.isLoading,
                onClick = onRegistrarClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onNavigateBack) {
                Text("¿Ya tienes una cuenta? Inicia sesión aquí", color = ItsurOnSurface, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    AppTheme {
        RegisterContent(
            uiState = RegisterUiState(),
            onNavigateBack = {},
            onNombresChange = {},
            onApellidosChange = {},
            onNombreUsuarioChange = {},
            onCurpChange = {},
            onEmailChange = {},
            onTelefonoChange = {},
            onCarreraChange = {},
            onRecargarCarreras = {},
            onEstadoChange = {},
            onMunicipioChange = {},
            onContrasenaChange = {},
            onConfirmarContrasenaChange = {},
            onRegistrarClick = {}
        )
    }
}
