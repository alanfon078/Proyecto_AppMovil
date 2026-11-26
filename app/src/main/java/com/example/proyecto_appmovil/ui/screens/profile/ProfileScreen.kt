package com.example.proyecto_appmovil.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.*
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    ProfileContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onToggleEdit = viewModel::toggleEditMode,
        onTelefonoChange = viewModel::onTelefonoChange,
        onDireccionChange = viewModel::onDireccionChange,
        onEstadoChange = viewModel::onEstadoChange,
        onMunicipioChange = viewModel::onMunicipioChange,
        onGuardarCambios = viewModel::guardarCambios,
        onPasswordActualChange = viewModel::onPasswordActualChange,
        onConfirmacionPasswordActualChange = viewModel::onConfirmacionPasswordActualChange,
        onPasswordNuevaChange = viewModel::onPasswordNuevaChange,
        onCambiarPassword = viewModel::cambiarPassword
    )
}

@Composable
fun ProfileContent(
    uiState: ProfileUiState,
    onNavigateBack: () -> Unit,
    onToggleEdit: () -> Unit,
    onTelefonoChange: (String) -> Unit,
    onDireccionChange: (String) -> Unit,
    onEstadoChange: (String) -> Unit,
    onMunicipioChange: (String) -> Unit,
    onGuardarCambios: () -> Unit,
    onPasswordActualChange: (String) -> Unit,
    onConfirmacionPasswordActualChange: (String) -> Unit,
    onPasswordNuevaChange: (String) -> Unit,
    onCambiarPassword: () -> Unit
) {
    val confirmacionPasswordFocus = remember { FocusRequester() }
    val passwordNuevaFocus = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val guardarPassword = {
        focusManager.clearFocus()
        onCambiarPassword()
    }

    Scaffold(
        modifier = Modifier.imePadding(),
        topBar = {
            ItsurTopBar(
                title = "Datos Personales",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de Identidad
            Card(
                colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(color = ItsurOnSurface, shape = RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = uiState.aspirante?.nombreCompleto ?: "Aspirante",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ItsurVinoOscuro
                            )
                            Text(
                                text = "Folio: ${uiState.aspirante?.folioAspirante ?: ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ItsurOnSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.mensajeExito != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = uiState.mensajeExito ?: "",
                        color = ItsurOnSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            if (uiState.mensajeError != null) {
                ErrorBanner(mensaje = uiState.mensajeError ?: "")
            }

            // Datos Oficiales (Lectura)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Información Oficial",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    DatoItem(label = "CURP", valor = uiState.aspirante?.curp ?: "")
                    DatoItem(label = "Correo Electrónico", valor = uiState.aspirante?.email ?: "")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Datos de Contacto (Lectura o Edición)
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Contacto y Residencia",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        TextButton(onClick = onToggleEdit, enabled = uiState.aspirante != null && !uiState.isLoading) {
                            Text(
                                text = if (uiState.isEditMode) "Cancelar" else "Editar",
                                color = ItsurOnSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (uiState.isEditMode) {
                        ItsurTextField(
                            value = uiState.telefonoEdit,
                            onValueChange = onTelefonoChange,
                            label = "Teléfono de Contacto",
                            leadingIcon = Icons.Default.Phone
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ItsurTextField(
                            value = uiState.direccionEdit,
                            onValueChange = onDireccionChange,
                            label = "Dirección (calle y colonia)",
                            leadingIcon = Icons.Default.Home
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ItsurTextField(
                            value = uiState.municipioEdit,
                            onValueChange = onMunicipioChange,
                            label = "Municipio",
                            leadingIcon = Icons.Default.LocationOn
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        ItsurTextField(
                            value = uiState.estadoEdit,
                            onValueChange = onEstadoChange,
                            label = "Estado",
                            leadingIcon = Icons.Default.LocationOn
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        PrimaryButton(
                            text = "Guardar Cambios",
                            isLoading = uiState.isLoading,
                            onClick = onGuardarCambios
                        )
                    } else {
                        DatoItem(label = "Teléfono", valor = uiState.aspirante?.telefono ?: "")
                        DatoItem(label = "Dirección (calle y colonia)", valor = uiState.aspirante?.direccion.orEmpty())
                        DatoItem(label = "Municipio", valor = uiState.aspirante?.municipioResidencia.orEmpty())
                        DatoItem(label = "Estado", valor = uiState.aspirante?.estadoResidencia.orEmpty())
                        DatoItem(label = "Carrera Asignada", valor = uiState.aspirante?.carrera ?: "")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cambio de Contraseña
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Seguridad y Contraseña",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    ItsurTextField(
                        value = uiState.passwordActual,
                        onValueChange = onPasswordActualChange,
                        label = "Contraseña Actual",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { confirmacionPasswordFocus.requestFocus() })
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ItsurTextField(
                        value = uiState.confirmacionPasswordActual,
                        onValueChange = onConfirmacionPasswordActualChange,
                        label = "Confirmar Contraseña Actual",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        modifier = Modifier.focusRequester(confirmacionPasswordFocus),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { passwordNuevaFocus.requestFocus() })
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ItsurTextField(
                        value = uiState.passwordNueva,
                        onValueChange = onPasswordNuevaChange,
                        label = "Nueva Contraseña (mín. 8 caracteres)",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        modifier = Modifier.focusRequester(passwordNuevaFocus),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { guardarPassword() })
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    uiState.errorPassword?.let { ErrorBanner(mensaje = it) }
                    if (uiState.passwordActualizada) {
                        Text(
                            text = "Contraseña cambiada con éxito",
                            color = ItsurOnSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    PrimaryButton(
                        text = "Actualizar Contraseña",
                        enabled = uiState.passwordActual.isNotEmpty() &&
                            uiState.confirmacionPasswordActual.isNotEmpty() && uiState.passwordNueva.isNotEmpty(),
                        isLoading = uiState.isLoading,
                        onClick = guardarPassword
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regresar al Menú Principal", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DatoItem(label: String, valor: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = ItsurOnSurfaceVariant)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = ItsurVinoOscuro)
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    AppTheme {
        ProfileContent(
            uiState = ProfileUiState(),
            onNavigateBack = {},
            onToggleEdit = {},
            onTelefonoChange = {},
            onDireccionChange = {},
            onEstadoChange = {},
            onMunicipioChange = {},
            onGuardarCambios = {},
            onPasswordActualChange = {},
            onConfirmacionPasswordActualChange = {},
            onPasswordNuevaChange = {},
            onCambiarPassword = {}
        )
    }
}
