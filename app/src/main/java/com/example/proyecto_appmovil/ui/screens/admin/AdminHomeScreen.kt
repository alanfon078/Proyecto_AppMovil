package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.components.itsurTextFieldColors
import com.example.proyecto_appmovil.ui.theme.ItsurOffWhite
import com.example.proyecto_appmovil.ui.theme.ItsurOnSurface
import com.example.proyecto_appmovil.ui.theme.ItsurOnSurfaceVariant
import com.example.proyecto_appmovil.ui.theme.ItsurRosaPastel
import com.example.proyecto_appmovil.ui.theme.ItsurVinoOscuro
import com.example.proyecto_appmovil.ui.theme.StatusAprobadoBg
import com.example.proyecto_appmovil.ui.theme.StatusPendienteBg

@Composable
fun AdminHomeScreen(
    onNavigateToRoute: (String) -> Unit,
    onNavigateToAspiranteDetail: (String) -> Unit = {},
    onLogout: () -> Unit,
    viewModel: AdminHomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(Unit) { viewModel.cargarAspirantes() }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.cargarAspirantes()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AdminHomeContent(
        uiState = uiState,
        onNavigateToRoute = onNavigateToRoute,
        onNavigateToAspiranteDetail = onNavigateToAspiranteDetail,
        onLogout = onLogout,
        onBusquedaChange = viewModel::onBusquedaChange,
        onFiltroEstadoChange = viewModel::onFiltroEstadoChange
    )
}

@Composable
fun AdminHomeContent(
    uiState: AdminHomeUiState,
    onNavigateToRoute: (String) -> Unit,
    onNavigateToAspiranteDetail: (String) -> Unit,
    onLogout: () -> Unit,
    onBusquedaChange: (String) -> Unit,
    onFiltroEstadoChange: (EstadoInscripcion?) -> Unit
) {
    AdminDrawerScaffold(
        title = "Panel de Control Escolar",
        currentRoute = Screen.AdminHome.route,
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            uiState.mensajeError?.let { ErrorBanner(mensaje = it) }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                item {
                    ContadorCard("Total", uiState.totalAspirantes, null, uiState.estadoFiltroSeleccionado == null, ItsurRosaPastel, onFiltroEstadoChange)
                }
                item {
                    ContadorCard("En Revisión", uiState.inRevision, EstadoInscripcion.EN_REVISION, uiState.estadoFiltroSeleccionado == EstadoInscripcion.EN_REVISION, StatusPendienteBg, onFiltroEstadoChange)
                }
                item {
                    ContadorCard("Aprobados", uiState.aprobados, EstadoInscripcion.ACEPTADO, uiState.estadoFiltroSeleccionado == EstadoInscripcion.ACEPTADO, StatusAprobadoBg, onFiltroEstadoChange)
                }
                item {
                    ContadorCard("En Captura", uiState.enCaptura, EstadoInscripcion.PENDIENTE, uiState.estadoFiltroSeleccionado == EstadoInscripcion.PENDIENTE, StatusPendienteBg, onFiltroEstadoChange)
                }
                item {
                    ContadorCard("Con Observaciones", uiState.conObservaciones, EstadoInscripcion.CON_OBSERVACIONES, uiState.estadoFiltroSeleccionado == EstadoInscripcion.CON_OBSERVACIONES, StatusPendienteBg, onFiltroEstadoChange)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.filtroBusqueda,
                onValueChange = onBusquedaChange,
                label = { Text("Buscar por nombre, folio o CURP") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                colors = itsurTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Aspirantes Registrados (${uiState.aspirantes.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ItsurVinoOscuro
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(uiState.aspirantes, key = { it.id }) { aspirante ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(aspirante.nombreCompleto, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = ItsurVinoOscuro)
                                    Text("Folio: ${aspirante.folioAspirante} · ${aspirante.carrera}", style = MaterialTheme.typography.bodySmall, color = ItsurOnSurfaceVariant)
                                }
                                StatusChip(status = aspirante.estadoInscripcion.name)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { onNavigateToAspiranteDetail(aspirante.id) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = ItsurVinoOscuro)
                            ) {
                                Text("Ver Expediente", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContadorCard(
    titulo: String,
    cantidad: Int,
    estado: EstadoInscripcion?,
    selected: Boolean,
    containerColor: androidx.compose.ui.graphics.Color,
    onFiltroEstadoChange: (EstadoInscripcion?) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .width(118.dp)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) ItsurVinoOscuro else ItsurVinoOscuro.copy(alpha = 0.15f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onFiltroEstadoChange(estado) }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = ItsurOnSurface)
            Text(cantidad.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ItsurVinoOscuro)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminHomeScreenPreview() {
    AdminHomeContent(
        uiState = AdminHomeUiState(),
        onNavigateToRoute = {},
        onNavigateToAspiranteDetail = {},
        onLogout = {},
        onBusquedaChange = {},
        onFiltroEstadoChange = {}
    )
}
