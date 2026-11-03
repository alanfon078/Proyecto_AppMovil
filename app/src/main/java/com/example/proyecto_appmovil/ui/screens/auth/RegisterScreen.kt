package com.example.proyecto_appmovil.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
        carrerasItsur = viewModel.carrerasItsur,
        municipiosGto = viewModel.municipiosGto,
        onNavigateBack = onNavigateBack,
        onNombresChange = viewModel::onNombresChange,
        onApellidosChange = viewModel::onApellidosChange,
        onCurpChange = viewModel::onCurpChange,
        onEmailChange = viewModel::onEmailChange,
        onTelefonoChange = viewModel::onTelefonoChange,
        onCarreraChange = viewModel::onCarreraChange,
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
    carrerasItsur: List<String>,
    municipiosGto: List<String>,
    onNavigateBack: () -> Unit,
    onNombresChange: (String) -> Unit,
    onApellidosChange: (String) -> Unit,
    onCurpChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onCarreraChange: (String) -> Unit,
    onMunicipioChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onConfirmarContrasenaChange: (String) -> Unit,
    onRegistrarClick: () -> Unit
) {
    var carreraExpanded by remember { mutableStateOf(false) }
    var municipioExpanded by remember { mutableStateOf(false) }

    Scaffold(
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
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
                onExpandedChange = { carreraExpanded = !carreraExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.carrera,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Carrera de Interés") },
                    leadingIcon = {
                        Icon(Icons.Default.School, contentDescription = null, tint = ItsurCiruela)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = carreraExpanded)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ItsurCiruela,
                        focusedLabelColor = ItsurCiruela
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = carreraExpanded,
                    onDismissRequest = { carreraExpanded = false }
                ) {
                    carrerasItsur.forEach { carrera ->
                        DropdownMenuItem(
                            text = { Text(carrera) },
                            onClick = {
                                onCarreraChange(carrera)
                                carreraExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Municipio
            ExposedDropdownMenuBox(
                expanded = municipioExpanded,
                onExpandedChange = { municipioExpanded = !municipioExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "${uiState.municipio}, ${uiState.estado}",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Municipio / Estado de Residencia") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ItsurCiruela)
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = municipioExpanded)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ItsurCiruela,
                        focusedLabelColor = ItsurCiruela
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = municipioExpanded,
                    onDismissRequest = { municipioExpanded = false }
                ) {
                    municipiosGto.forEach { muni ->
                        DropdownMenuItem(
                            text = { Text("$muni, Guanajuato") },
                            onClick = {
                                onMunicipioChange(muni)
                                municipioExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            ItsurTextField(
                value = uiState.contrasena,
                onValueChange = onContrasenaChange,
                label = "Contraseña (mínimo 6 caracteres)",
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
                Text("¿Ya tienes una cuenta? Inicia sesión aquí", color = ItsurCiruela, fontWeight = FontWeight.Bold)
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
            carrerasItsur = listOf("Ing. Sistemas Computacionales", "Ing. Industrial"),
            municipiosGto = listOf("Uriangato", "Moroleón"),
            onNavigateBack = {},
            onNombresChange = {},
            onApellidosChange = {},
            onCurpChange = {},
            onEmailChange = {},
            onTelefonoChange = {},
            onCarreraChange = {},
            onMunicipioChange = {},
            onContrasenaChange = {},
            onConfirmarContrasenaChange = {},
            onRegistrarClick = {}
        )
    }
}
