package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.components.itsurTextFieldColors
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun AdminHomeScreen(
    onNavigateToFechas: () -> Unit,
    onNavigateToAspiranteDetail: (String) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: AdminHomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.cargarAspirantes() }

    AdminHomeContent(
        uiState = uiState,
        onNavigateToFechas = onNavigateToFechas,
        onNavigateToAspiranteDetail = onNavigateToAspiranteDetail,
        onLogout = onLogout,
        onBusquedaChange = viewModel::onBusquedaChange,
        onAbrirRevision = viewModel::abrirDialogoRevision,
        onCerrarRevision = viewModel::cerrarDialogoRevision,
        onSeleccionarDocumento = viewModel::seleccionarDocumento,
        onObservacionesChange = viewModel::onObservacionesChange,
        onResolverRevision = viewModel::resolverRevision
    )
}

@Composable
fun AdminHomeContent(
    uiState: AdminHomeUiState,
    onNavigateToFechas: () -> Unit,
    onNavigateToAspiranteDetail: (String) -> Unit,
    onLogout: () -> Unit,
    onBusquedaChange: (String) -> Unit,
    onAbrirRevision: (Aspirante) -> Unit,
    onCerrarRevision: () -> Unit,
    onSeleccionarDocumento: (Documento) -> Unit,
    onObservacionesChange: (String) -> Unit,
    onResolverRevision: (Boolean) -> Unit
) {
    val aspirantesFiltrados = remember(uiState.aspirantes, uiState.filtroBusqueda) {
        val q = uiState.filtroBusqueda.trim().lowercase()
        if (q.isBlank()) uiState.aspirantes else {
            uiState.aspirantes.filter {
                it.nombreCompleto.lowercase().contains(q) || it.folioAspirante.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Panel de Control Escolar",
                onLogout = onLogout
            )
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

            if (uiState.isLoading && uiState.aspiranteSeleccionadoParaRevision == null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (uiState.aspiranteSeleccionadoParaRevision == null) {
                uiState.mensajeError?.let { ErrorBanner(mensaje = it) }
                uiState.mensajeExito?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }

            // Tarjetas de Métricas de Control Escolar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Total", style = MaterialTheme.typography.labelMedium, color = ItsurVinoOscuro)
                        Text("${uiState.aspirantes.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ItsurVinoOscuro)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusPendienteBg),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Revisión", style = MaterialTheme.typography.labelMedium, color = ItsurOnSurface)
                        Text("${uiState.aspirantes.count { it.estadoInscripcion.name == "EN_REVISION" }}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ItsurOnSurface)
                    }
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Aprobados", style = MaterialTheme.typography.labelMedium, color = ItsurOnSurface)
                        Text("${uiState.aspirantes.count { it.estadoInscripcion.name == "ACEPTADO" }}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ItsurOnSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buscador por nombre o folio
            OutlinedTextField(
                value = uiState.filtroBusqueda,
                onValueChange = onBusquedaChange,
                label = { Text("Buscar por nombre o número de folio") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ItsurCiruela) },
                shape = RoundedCornerShape(12.dp),
                colors = itsurTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aspirantes Registrados (${aspirantesFiltrados.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ItsurVinoOscuro
                )
                TextButton(onClick = onNavigateToFechas) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fechas Límite", color = ItsurOnSurface, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(aspirantesFiltrados, key = { it.id }) { asp ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = asp.nombreCompleto,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = ItsurVinoOscuro
                                    )
                                    Text(
                                        text = "Folio: ${asp.folioAspirante} • ${asp.carrera}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ItsurOnSurfaceVariant
                                    )
                                }
                                StatusChip(status = asp.estadoInscripcion.name)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { onNavigateToAspiranteDetail(asp.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ItsurRosaPastel,
                                        contentColor = ItsurVinoOscuro
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Ver Expediente", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { onAbrirRevision(asp) },
                                    enabled = !uiState.isLoading && !uiState.cargandoDocumentos,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = ItsurCiruela,
                                        contentColor = Color.White,
                                        disabledContentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Revisión Rápida", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // Diálogo interactivo para aprobar / rechazar con observaciones obligatorias
        if (uiState.aspiranteSeleccionadoParaRevision != null) {
            val asp = uiState.aspiranteSeleccionadoParaRevision
            AlertDialog(
                onDismissRequest = onCerrarRevision,
                title = {
                    Text(
                        text = "Revisión de ${asp.nombreCompleto}",
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "Folio: ${asp.folioAspirante} • ${asp.carrera}",
                            style = MaterialTheme.typography.bodySmall,
                            color = ItsurOnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        uiState.mensajeError?.let { ErrorBanner(mensaje = it) }
                        when {
                            uiState.cargandoDocumentos -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            uiState.documentosParaRevision.isEmpty() && uiState.mensajeError == null ->
                                Text("No hay documentos pendientes de revisión.")
                            uiState.documentosParaRevision.isNotEmpty() -> {
                                Column(Modifier.selectableGroup()) {
                                    uiState.documentosParaRevision.forEach { doc ->
                                        val seleccionado = doc == uiState.documentoSeleccionado
                                        Row(
                                            modifier = Modifier.fillMaxWidth().selectable(
                                                selected = seleccionado,
                                                onClick = { onSeleccionarDocumento(doc) },
                                                enabled = !uiState.isLoading,
                                                role = Role.RadioButton
                                            ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(selected = seleccionado, onClick = null)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(doc.tipo.titulo)
                                        }
                                    }
                                }
                                OutlinedTextField(
                                    value = uiState.observacionesInput,
                                    onValueChange = onObservacionesChange,
                                    enabled = !uiState.isLoading,
                                    label = { Text("Observaciones / Motivo de rechazo") },
                                    colors = itsurTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onResolverRevision(true) },
                        enabled = !uiState.isLoading && uiState.documentoSeleccionado != null,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusAprobado, disabledContentColor = Color.Black)
                    ) {
                        Text("Aprobar")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { onResolverRevision(false) },
                        enabled = !uiState.isLoading && uiState.documentoSeleccionado != null,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRechazado, disabledContentColor = Color.Black)
                    ) {
                        Text("Rechazar")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    AppTheme {
        AdminHomeContent(
            uiState = AdminHomeUiState(),
            onNavigateToFechas = {},
            onNavigateToAspiranteDetail = {},
            onLogout = {},
            onBusquedaChange = {},
            onAbrirRevision = {},
            onCerrarRevision = {},
            onSeleccionarDocumento = {},
            onObservacionesChange = {},
            onResolverRevision = {}
        )
    }
}
