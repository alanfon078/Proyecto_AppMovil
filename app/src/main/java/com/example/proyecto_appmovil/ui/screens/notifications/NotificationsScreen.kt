package com.example.proyecto_appmovil.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.proyecto_appmovil.model.Notificacion
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.EmptyState
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    NotificationsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSetTab = viewModel::setTab,
        onToggleSoloNoLeidas = viewModel::toggleSoloNoLeidas,
        onMarcarLeida = viewModel::marcarComoLeida
    )
}

@Composable
fun NotificationsContent(
    uiState: NotificationsUiState,
    onNavigateBack: () -> Unit,
    onSetTab: (Int) -> Unit,
    onToggleSoloNoLeidas: () -> Unit,
    onMarcarLeida: (String) -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Notificaciones y Fechas",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = uiState.tabSeleccionada,
                containerColor = ItsurRosaPastel,
                contentColor = ItsurVinoOscuro
            ) {
                Tab(
                    selected = uiState.tabSeleccionada == 0,
                    onClick = { onSetTab(0) },
                    text = {
                        Text(
                            text = "Avisos (${uiState.notificaciones.count { !it.leida }} nuevos)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = uiState.tabSeleccionada == 1,
                    onClick = { onSetTab(1) },
                    text = {
                        Text(
                            text = "Fechas Límite",
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            if (uiState.tabSeleccionada == 0) {
                // Pestaña de Avisos
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toca un aviso para marcarlo como leído",
                        style = MaterialTheme.typography.bodySmall,
                        color = ItsurOnSurfaceVariant
                    )
                    FilterChip(
                        selected = uiState.soloNoLeidas,
                        onClick = onToggleSoloNoLeidas,
                        label = { Text("No leídas") }
                    )
                }

                val lista = if (uiState.soloNoLeidas) {
                    uiState.notificaciones.filter { !it.leida }
                } else {
                    uiState.notificaciones
                }

                if (lista.isEmpty()) {
                    EmptyState(mensaje = "No hay notificaciones en esta sección")
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista, key = { it.id }) { notif ->
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.leida) Color.White else ItsurRosaSuave
                                ),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMarcarLeida(notif.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    if (!notif.leida) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 4.dp, end = 12.dp)
                                                .size(10.dp)
                                                .background(ItsurCiruela, CircleShape)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.titulo,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = ItsurVinoOscuro
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = notif.mensaje,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ItsurOnSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = notif.fecha,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ItsurOutline
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Pestaña de Fechas Límite
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
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
                                        .size(44.dp)
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
                                        text = "Fecha: ${fecha.fechaLimite}",
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

            // Botón de regresar al menú en la parte inferior
            Box(modifier = Modifier.padding(20.dp)) {
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
                    Text("Regresar al Menú Principal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotificationsScreenPreview() {
    AppTheme {
        NotificationsContent(
            uiState = NotificationsUiState(
                notificaciones = listOf(
                    Notificacion(
                        id = "1",
                        titulo = "Revisión completada",
                        mensaje = "Tus documentos han sido aprobados por Control Escolar.",
                        fecha = "Hoy, 10:30 AM",
                        leida = false
                    )
                ),
                fechas = listOf(
                    FechaImportante("1", "Cierre de Convocatoria", "Entrega de documentos oficiales", "30 de Junio 2026", 15)
                )
            ),
            onNavigateBack = {},
            onSetTab = {},
            onToggleSoloNoLeidas = {},
            onMarcarLeida = {}
        )
    }
}
