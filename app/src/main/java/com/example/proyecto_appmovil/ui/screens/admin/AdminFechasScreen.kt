package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.FechaImportante
import com.example.proyecto_appmovil.model.TipoFechaImportante
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.EmptyState
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.itsurTextFieldColors
import com.example.proyecto_appmovil.ui.theme.ItsurCiruela
import com.example.proyecto_appmovil.ui.theme.ItsurOnSurface
import com.example.proyecto_appmovil.ui.theme.ItsurOnSurfaceVariant
import com.example.proyecto_appmovil.ui.theme.ItsurVinoOscuro
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun AdminFechasScreen(
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminFechasViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    AdminFechasContent(
        uiState = uiState,
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout,
        onAbrirDialogo = viewModel::abrirDialogoNuevaFecha,
        onAbrirEditar = viewModel::abrirDialogoEditar,
        onEliminar = viewModel::eliminarFecha,
        onCerrarDialogo = viewModel::cerrarDialogoNuevaFecha,
        onTituloChange = viewModel::onTituloChange,
        onDescripcionChange = viewModel::onDescripcionChange,
        onFechaLimiteChange = viewModel::onFechaLimiteChange,
        onTipoChange = viewModel::onTipoChange,
        onGuardar = viewModel::guardarFecha
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFechasContent(
    uiState: AdminFechasUiState,
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit,
    onAbrirDialogo: () -> Unit,
    onAbrirEditar: (FechaImportante) -> Unit,
    onEliminar: (String) -> Unit,
    onCerrarDialogo: () -> Unit,
    onTituloChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onFechaLimiteChange: (String) -> Unit,
    onTipoChange: (TipoFechaImportante) -> Unit,
    onGuardar: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    AdminDrawerScaffold(
        title = "Fechas Límite",
        currentRoute = Screen.AdminFechas.route,
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Fechas Programadas (${uiState.fechas.size})",
                    modifier = Modifier.weight(1f),
                    color = ItsurVinoOscuro,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onAbrirDialogo, colors = ButtonDefaults.buttonColors(containerColor = ItsurCiruela)) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("+ Nueva Fecha")
                }
            }
            if (uiState.isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            uiState.mensajeError?.let { ErrorBanner(mensaje = it) }
            uiState.mensajeExito?.let { Text(it, color = ItsurOnSurface, modifier = Modifier.padding(vertical = 8.dp)) }

            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.fechas.isEmpty() && !uiState.isLoading) {
                EmptyState(mensaje = "No hay fechas límite registradas")
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.fechas, key = { it.id }) { fecha ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(fecha.titulo, color = ItsurVinoOscuro, fontWeight = FontWeight.Bold)
                                    Text(fecha.descripcion, color = ItsurOnSurfaceVariant)
                                    Text("Tipo: ${TipoFechaImportante.fromApi(fecha.tipo).etiqueta}", color = ItsurOnSurface)
                                    Text("Límite: ${fecha.fechaLimite}", color = ItsurOnSurface)
                                    Text("${fecha.diasRestantes} días restantes", color = ItsurOnSurfaceVariant)
                                }
                                IconButton(onClick = { onAbrirEditar(fecha) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                                }
                                IconButton(onClick = { onEliminar(fecha.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (uiState.showDialogNuevaFecha) {
        AlertDialog(
            onDismissRequest = onCerrarDialogo,
            title = { Text(if (uiState.fechaEnEdicionId == null) "Nueva fecha importante" else "Editar fecha importante") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.nuevoTitulo,
                        onValueChange = onTituloChange,
                        label = { Text("Título") },
                        colors = itsurTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.nuevaDescripcion,
                        onValueChange = onDescripcionChange,
                        label = { Text("Descripción") },
                        colors = itsurTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    TipoFechaImportante.entries.forEach { tipo ->
                        OutlinedButton(
                            onClick = { onTipoChange(tipo) },
                            colors = if (uiState.nuevoTipo == tipo)
                                ButtonDefaults.outlinedButtonColors(containerColor = ItsurCiruela.copy(alpha = 0.12f))
                            else ButtonDefaults.outlinedButtonColors(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(tipo.etiqueta, color = ItsurOnSurface) }
                    }
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.nuevaFechaLimite.isBlank()) "Seleccionar fecha" else uiState.nuevaFechaLimite)
                    }
                    uiState.mensajeError?.let { ErrorBanner(mensaje = it) }
                }
            },
            confirmButton = {
                Button(onClick = onGuardar, enabled = !uiState.isLoading) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = onCerrarDialogo) { Text("Cancelar") }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                        onFechaLimiteChange(fecha)
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
