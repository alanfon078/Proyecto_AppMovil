package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ItsurTextField

@Composable
fun AdminBroadcastScreen(
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminBroadcastViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    AdminDrawerScaffold(
        title = "Aviso General",
        currentRoute = Screen.AdminBroadcast.route,
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ItsurTextField(
                        value = state.titulo,
                        onValueChange = viewModel::onTituloChange,
                        label = "Título del aviso"
                    )
                    ItsurTextField(
                        value = state.mensaje,
                        onValueChange = viewModel::onMensajeChange,
                        label = "Mensaje para todos los aspirantes",
                        singleLine = false
                    )
                    state.mensajeError?.let { Text(it, color = Color.Black) }
                    state.mensajeExito?.let {
                        Text(it, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = viewModel::solicitarEnvio,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Enviar Aviso General", fontWeight = FontWeight.Bold) }
                }
            }
        }
    }

    if (state.showConfirmacion) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEnvio,
            title = { Text("Confirmar aviso general", color = Color.Black) },
            text = { Text("El aviso llegará a todos los aspirantes activos registrados.", color = Color.Black) },
            confirmButton = { TextButton(onClick = viewModel::enviar) { Text("Enviar", color = Color.Black) } },
            dismissButton = { TextButton(onClick = viewModel::cancelarEnvio) { Text("Cancelar", color = Color.Black) } }
        )
    }
}
