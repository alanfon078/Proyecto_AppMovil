package com.example.proyecto_appmovil.ui.screens.questions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.CategoriaPregunta
import com.example.proyecto_appmovil.model.EstadoPregunta
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ItsurTextField
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.theme.ItsurOffWhite
import androidx.compose.ui.platform.LocalContext
import com.example.proyecto_appmovil.work.NotificacionesWorkManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentQuestionsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentQuestionsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    LaunchedEffect(Unit) { NotificacionesWorkManager.syncNow(context) }

    Scaffold(
        topBar = { ItsurTopBar(title = "Preguntas y Dudas", onNavigateBack = onNavigateBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                            OutlinedTextField(
                                value = state.categoria.etiqueta,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Categoría") },
                                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                )
                            )
                            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                CategoriaPregunta.entries.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria.etiqueta, color = Color.Black) },
                                        onClick = {
                                            viewModel.onCategoriaChange(categoria)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        ItsurTextField(
                            value = state.texto,
                            onValueChange = viewModel::onTextoChange,
                            label = "Redacta tu duda",
                            singleLine = false,
                            errorMessage = state.mensajeError,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = viewModel::enviar,
                            enabled = !state.isLoading,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Enviar Pregunta", fontWeight = FontWeight.Bold) }
                        state.mensajeExito?.let { Text(it, color = Color(0xFF1B5E20), fontWeight = FontWeight.Bold) }
                    }
                }
            }
            items(state.preguntas, key = { it.id }) { pregunta ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(pregunta.categoria.etiqueta, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text(pregunta.pregunta, color = Color.Black)
                        AssistChip(
                            onClick = {},
                            label = {
                                Text(
                                    if (pregunta.estado == EstadoPregunta.RESPONDIDA) "Respondida" else "Pendiente de respuesta",
                                    color = Color.Black
                                )
                            }
                        )
                        if (pregunta.respuesta != null) {
                            HorizontalDivider()
                            Text("Respuesta del administrador", fontWeight = FontWeight.Bold, color = Color.Black)
                            Text(pregunta.respuesta, color = Color.Black)
                            pregunta.fechaRespuesta?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Black) }
                        }
                        Text(pregunta.fechaCreacion, style = MaterialTheme.typography.labelSmall, color = Color.Black)
                    }
                }
            }
        }
    }
}
