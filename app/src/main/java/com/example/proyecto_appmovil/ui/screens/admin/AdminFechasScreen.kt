package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.EmptyState
import com.example.proyecto_appmovil.ui.components.ItsurTextField
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.PrimaryButton
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun AdminFechasScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminFechasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    AdminFechasContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onAbrirDialogo = viewModel::abrirDialogoNuevaFecha,
        onCerrarDialogo = viewModel::cerrarDialogoNuevaFecha,
        onTituloChange = viewModel::onTituloChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onFechaLimiteChange = viewModel::onFechaLimiteChange,
        onDiasRestantesChange = viewModel::onDiasRestantesChange,
        onGuardar = viewModel::guardarNuevaFecha
    )
}

@Composable
fun AdminFechasContent(
    uiState: AdminFechasUiState,
    onNavigateBack: () -> Unit,
    onAbrirDialogo: () -> Unit,
    onCerrarDialogo: () -> Unit,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaLimiteChange: (String) -> Unit,
    onDiasRestantesChange: (String) -> Unit,
    onGuardar: () -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Calendario y Fechas Límite",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAbrirDialogo,
                containerColor = ItsurCiruela,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Programar Fecha")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Banner explicativo
            Card(
                colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = ItsurVinoOscuro,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Control de Convocatoria ITSUR",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        Text(
                            text = "Gestiona las fechas límite oficiales visibles en la app para todos los aspirantes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ItsurVinoOscuro
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.mensajeExito != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.mensajeExito,
                        color = StatusAprobado,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Text(
                text = "Fechas Programadas (${uiState.fechas.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ItsurVinoOscuro
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.fechas.isEmpty()) {
                EmptyState(mensaje = "No hay fechas límite registradas")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.fechas, key = { it.id }) { fecha ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .background(ItsurRosaPastel, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = ItsurVinoOscuro
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = fecha.titulo,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ItsurVinoOscuro
                                    )
                                    Text(
                                        text = fecha.descripcion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ItsurOnSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Límite: ${fecha.fechaLimite}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ItsurCiruela
                                    )
                                }

                                Surface(
                                    color = if (fecha.diasRestantes <= 10) StatusPendienteBg else ItsurRosaSuave,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${fecha.diasRestantes} días",
                                        color = if (fecha.diasRestantes <= 10) StatusPendiente else ItsurVinoOscuro,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ItsurCiruela),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Regresar al Panel Escolar", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Diálogo para programar nueva fecha límite
        if (uiState.showDialogNuevaFecha) {
            AlertDialog(
                onDismissRequest = onCerrarDialogo,
                title = {
                    Text(
                        text = "Programar Fecha Límite",
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ItsurTextField(
                            value = uiState.nuevoTitulo,
                            onValueChange = onTituloChange,
                            label = "Título del evento o entrega"
                        )
                        ItsurTextField(
                            value = uiState.nuevaDescripcion,
                            onValueChange = onDescripcionChange,
                            label = "Descripción"
                        )
                        ItsurTextField(
                            value = uiState.nuevaFechaLimite,
                            onValueChange = onFechaLimiteChange,
                            label = "Fecha Límite (ej. 20 de Julio 2026)"
                        )
                        ItsurTextField(
                            value = uiState.nuevosDiasRestantes,
                            onValueChange = onDiasRestantesChange,
                            label = "Días restantes aproximados"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = onGuardar,
                        colors = ButtonDefaults.buttonColors(containerColor = ItsurCiruela)
                    ) {
                        Text("Guardar Fecha")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onCerrarDialogo) {
                        Text("Cancelar", color = ItsurOutline)
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminFechasPreview() {
    AppTheme {
        AdminFechasContent(
            uiState = AdminFechasUiState(
                fechas = listOf(
                    FechaImportante("1", "Cierre de Carga", "Fin del periodo de recepción", "15 de Junio 2026", 12),
                    FechaImportante("2", "Examen CENEVAL", "Aplicación presencial en campus", "20 de Junio 2026", 17)
                )
            ),
            onNavigateBack = {},
            onAbrirDialogo = {},
            onCerrarDialogo = {},
            onTituloChange = {},
            onDescripcionChange = {},
            onFechaLimiteChange = {},
            onDiasRestantesChange = {},
            onGuardar = {}
        )
    }
}
