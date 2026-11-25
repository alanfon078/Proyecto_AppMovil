package com.example.proyecto_appmovil.ui.screens.status

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun StatusScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: () -> Unit,
    viewModel: StatusViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarEstatus()
    }

    StatusContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToDocuments = onNavigateToDocuments
    )
}

@Composable
fun StatusContent(
    uiState: StatusUiState,
    onNavigateBack: () -> Unit,
    onNavigateToDocuments: () -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Estatus de Inscripción",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                uiState.mensajeError?.let { ErrorBanner(mensaje = it) }
            }

            uiState.aspirante?.let { aspirante ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estado General",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = ItsurVinoOscuro
                                )
                                StatusChip(status = aspirante.estadoInscripcion.name)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aspirante.estadoInscripcion.descripcion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ItsurVinoOscuro
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Línea de Tiempo del Trámite",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ItsurVinoOscuro
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val etapas = listOf(
                            "1. Registro de Aspirante",
                            "2. Carga de Documentos",
                            "3. Cotejo y Revisión Escolar",
                            "4. Documentos aprobados"
                        )

                        etapas.forEachIndexed { index, etapa ->
                            val etapaNum = index + 1
                            val isCompleted = etapaNum < uiState.etapaActual || uiState.etapaActual == etapas.size
                            val isCurrent = etapaNum == uiState.etapaActual && !isCompleted

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(
                                            color = when {
                                                isCompleted -> StatusAprobado
                                                isCurrent -> ItsurCiruela
                                                else -> Color(0xFFE0E0E0)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "$etapaNum",
                                            color = if (isCurrent) Color.White else Color.Black,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = etapa,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) ItsurVinoOscuro else ItsurOnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Documentos y Validación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )
                    TextButton(onClick = onNavigateToDocuments) {
                        Text("Ir a cargar", color = ItsurOnSurface, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(uiState.documentos, key = { it.tipo.name }) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = doc.tipo.titulo,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = ItsurVinoOscuro
                            )
                            if (doc.observaciones.isNotBlank()) {
                                Text(
                                    text = doc.observaciones,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ItsurOnSurfaceVariant
                                )
                            }
                        }
                        StatusChip(status = doc.estado.name)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
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
}

@Preview(showBackground = true)
@Composable
fun StatusScreenPreview() {
    AppTheme {
        StatusContent(
            uiState = StatusUiState(),
            onNavigateBack = {},
            onNavigateToDocuments = {}
        )
    }
}
