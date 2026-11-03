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
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.ui.AppViewModelProvider
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

                // Tarjeta de estado general
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
                            StatusChip(status = uiState.aspirante?.estadoInscripcion?.name ?: "EN_REVISION")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.aspirante?.estadoInscripcion?.descripcion ?: "En Proceso",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ItsurVinoOscuro
                        )
                    }
                }
            }

            item {
                // Stepper Visual de 5 Etapas
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
                            "4. Pago de Inscripción Acreditado",
                            "5. Inscripción Concluida"
                        )

                        etapas.forEachIndexed { index, etapa ->
                            val etapaNum = index + 1
                            val isCompleted = etapaNum < uiState.etapaActual
                            val isCurrent = etapaNum == uiState.etapaActual

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
                                            color = if (isCurrent) Color.White else Color(0xFF757575),
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
                        Text("Ir a cargar", color = ItsurCiruela, fontWeight = FontWeight.Bold)
                    }
                }
            }

            items(uiState.documentos, key = { it.id }) { doc ->
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
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ItsurCiruela),
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
            uiState = StatusUiState(
                aspirante = Aspirante(
                    nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
                    folioAspirante = "ITSUR-2026-0042",
                    estadoInscripcion = EstadoInscripcion.EN_REVISION
                ),
                etapaActual = 3,
                documentos = listOf(
                    Documento("1", TipoDocumento.ACTA_NACIMIENTO, estado = EstadoDocumento.VALIDADO),
                    Documento("2", TipoDocumento.CURP, estado = EstadoDocumento.SUBIDO)
                )
            ),
            onNavigateBack = {},
            onNavigateToDocuments = {}
        )
    }
}
