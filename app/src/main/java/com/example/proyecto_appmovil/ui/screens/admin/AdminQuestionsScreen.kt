package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ItsurTextField

@Composable
fun AdminQuestionsScreen(
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit,
    isExpanded: Boolean = false,
    viewModel: AdminQuestionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    AdminDrawerScaffold(
        title = "Preguntas y Dudas",
        currentRoute = Screen.AdminQuestions.route,
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout,
        isExpanded = isExpanded
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Todas", "fechas" to "Fechas", "documentos" to "Documentos").forEach { (value, label) ->
                    FilterChip(
                        selected = state.categoria == value,
                        onClick = { viewModel.setCategoria(value) },
                        label = { Text(label, color = Color.Black) }
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Todas", "pendiente" to "Pendientes por responder", "respondida" to "Respondidas").forEach { (value, label) ->
                    FilterChip(
                        selected = state.estado == value,
                        onClick = { viewModel.setEstado(value) },
                        label = { Text(label, color = Color.Black) }
                    )
                }
            }
            state.mensajeExito?.let { Text(it, color = Color.Black, fontWeight = FontWeight.Bold) }
            state.mensajeError?.let { Text(it, color = Color.Black) }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.preguntas, key = { it.id }) { pregunta ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(pregunta.nombreCompleto, fontWeight = FontWeight.Bold, color = Color.Black)
                            Text("${pregunta.folio} • ${pregunta.carrera}", color = Color.Black)
                            Text("Fecha: ${pregunta.fechaCreacion}", style = MaterialTheme.typography.labelSmall, color = Color.Black)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {}, label = { Text(pregunta.categoria.etiqueta, color = Color.Black) })
                                AssistChip(onClick = {}, label = { Text(pregunta.estado.etiqueta, color = Color.Black) })
                            }
                            Text(pregunta.pregunta, color = Color.Black)
                            pregunta.respuesta?.let {
                                HorizontalDivider()
                                Text("Respuesta: $it", color = Color.Black)
                            }
                            Button(onClick = { viewModel.abrirRespuesta(pregunta) }) {
                                Text(if (pregunta.respuesta == null) "Responder" else "Editar respuesta")
                            }
                        }
                    }
                }
            }
        }
    }

    if (state.showRespuesta) {
        AlertDialog(
            onDismissRequest = viewModel::cerrarRespuesta,
            title = { Text("Responder pregunta", color = Color.Black) },
            text = {
                ItsurTextField(
                    value = state.respuesta,
                    onValueChange = viewModel::onRespuestaChange,
                    label = "Respuesta",
                    singleLine = false,
                    errorMessage = state.mensajeError
                )
            },
            confirmButton = { TextButton(onClick = viewModel::responder) { Text("Enviar", color = Color.Black) } },
            dismissButton = { TextButton(onClick = viewModel::cerrarRespuesta) { Text("Cancelar", color = Color.Black) } }
        )
    }
}
