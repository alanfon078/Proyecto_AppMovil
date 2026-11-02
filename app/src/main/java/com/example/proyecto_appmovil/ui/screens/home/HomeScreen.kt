package com.example.proyecto_appmovil.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onNavigateToDocuments: () -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPhoto: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Panel del Aspirante", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToDocuments, modifier = Modifier.fillMaxWidth()) {
            Text("Cargar Documentos")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToStatus, modifier = Modifier.fillMaxWidth()) {
            Text("Estatus de Inscripción")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToProfile, modifier = Modifier.fillMaxWidth()) {
            Text("Datos Personales")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToNotifications, modifier = Modifier.fillMaxWidth()) {
            Text("Notificaciones y Fechas")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToPhoto, modifier = Modifier.fillMaxWidth()) {
            Text("Foto para Credencial")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onNavigateToPayment, modifier = Modifier.fillMaxWidth()) {
            Text("Comprobante de Pago")
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar Sesión")
        }
    }
}
