package com.example.proyecto_appmovil.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.example.proyecto_appmovil.ui.components.PrimaryButton
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToAdminHome: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LoginContent(
        uiState = uiState,
        onUsuarioChange = viewModel::onUsuarioChange,
        onContrasenaChange = viewModel::onContrasenaChange,
        onDemoAspirante = viewModel::setDemoAspirante,
        onDemoEscolar = viewModel::setDemoEscolar,
        onLoginClick = {
            viewModel.login { rol ->
                if (rol == "escolar") {
                    onNavigateToAdminHome()
                } else {
                    onNavigateToHome()
                }
            }
        },
        onNavigateToRegister = onNavigateToRegister
    )
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onUsuarioChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onDemoAspirante: () -> Unit,
    onDemoEscolar: () -> Unit,
    onLoginClick: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ItsurOffWhite)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Banner Institucional Rosa Pastel
        Card(
            colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = ItsurVinoOscuro,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "ITSUR",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ItsurVinoOscuro
                )
                Text(
                    text = "Inscripciones de Nuevo Ingreso",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ItsurVinoOscuro
                )
            }
        }

        Text(
            text = "Iniciar Sesión",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = ItsurVinoOscuro,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        if (uiState.errorMessage != null) {
            ErrorBanner(mensaje = uiState.errorMessage ?: "")
        }

        // Selector rápido de demostración
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDemoAspirante,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Demo Alumno", fontSize = 12.sp)
            }
            OutlinedButton(
                onClick = onDemoEscolar,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Demo Escolar", fontSize = 12.sp)
            }
        }

        ItsurTextField(
            value = uiState.usuario,
            onValueChange = onUsuarioChange,
            label = "Usuario o Correo institucional",
            leadingIcon = Icons.Default.Person,
            errorMessage = uiState.errorUsuario,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ItsurTextField(
            value = uiState.contrasena,
            onValueChange = onContrasenaChange,
            label = "Contraseña",
            leadingIcon = Icons.Default.Lock,
            isPassword = true,
            errorMessage = uiState.errorContrasena,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(28.dp))

        PrimaryButton(
            text = "Ingresar",
            isLoading = uiState.isLoading,
            onClick = onLoginClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(
                text = "¿No tienes cuenta? Regístrate aquí",
                color = ItsurCiruela,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    AppTheme {
        LoginContent(
            uiState = LoginUiState(usuario = "aspirante@itsur.edu.mx"),
            onUsuarioChange = {},
            onContrasenaChange = {},
            onDemoAspirante = {},
            onDemoEscolar = {},
            onLoginClick = {},
            onNavigateToRegister = {}
        )
    }
}
