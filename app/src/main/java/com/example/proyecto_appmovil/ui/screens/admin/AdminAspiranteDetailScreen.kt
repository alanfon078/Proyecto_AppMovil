package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.screens.profile.DatoItem
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun AdminAspiranteDetailScreen(
    aspiranteId: String,
    onNavigateBack: () -> Unit,
    viewModel: AdminAspiranteDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(aspiranteId) {
        viewModel.cargarDetalle(aspiranteId)
    }

    AdminAspiranteDetailContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onObservacionesChange = viewModel::onObservacionesChange,
        onValidarDoc = viewModel::validarDocumento,
        onAprobarAspirante = viewModel::aprobarAspirante,
        onPrevisualizarPdf = viewModel::descargarPdf
    )

    uiState.pdfPreviewFile?.let { file ->
        PdfViewerDialog(file = file, onDismiss = viewModel::cerrarPdfPreview)
    }
}

@Composable
fun AdminAspiranteDetailContent(
    uiState: AdminAspiranteDetailUiState,
    onNavigateBack: () -> Unit,
    onObservacionesChange: (String) -> Unit,
    onValidarDoc: (String, Boolean) -> Unit,
    onAprobarAspirante: () -> Unit,
    onPrevisualizarPdf: (String, String?) -> Unit
) {
    var documentoARechazar by remember { mutableStateOf<com.example.proyecto_appmovil.model.Documento?>(null) }

    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Expediente del Aspirante",
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

                // Card principal de aspirante
                val asp = uiState.aspirante
                Card(
                    colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(ItsurCiruela, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = asp?.nombreCompleto ?: "Aspirante",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ItsurVinoOscuro
                                    )
                                    Text(
                                        text = "Folio: ${asp?.folioAspirante ?: ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ItsurOnSurfaceVariant
                                    )
                                }
                            }
                            asp?.let { StatusChip(status = it.estadoInscripcion.name) }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ItsurVinoOscuro.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        DatoItem(label = "Carrera de Selección", valor = asp?.carrera ?: "")
                        DatoItem(label = "CURP", valor = asp?.curp ?: "")
                        DatoItem(label = "Correo Institucional", valor = asp?.email ?: "")
                        DatoItem(label = "Teléfono", valor = asp?.telefono ?: "")
                        DatoItem(label = "Promedio Bachillerato", valor = asp?.promedioBachillerato?.toString().orEmpty())
                        DatoItem(label = "Dirección (calle y colonia)", valor = asp?.direccion.orEmpty())
                        DatoItem(label = "Municipio", valor = asp?.municipioResidencia.orEmpty())
                        DatoItem(label = "Estado", valor = asp?.estadoResidencia.orEmpty())
                    }
                }
            }

            item {
                if (uiState.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                if (uiState.mensajeExito != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.mensajeExito,
                            color = ItsurOnSurface,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                if (uiState.mensajeError != null) {
                    ErrorBanner(mensaje = uiState.mensajeError)
                }
            }

            item {
                Text(
                    text = "Documentación Digital (${uiState.documentos.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ItsurVinoOscuro
                )
            }

            items(uiState.documentos, key = { it.tipo.name }) { doc ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = ItsurCiruela,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = doc.tipo.titulo,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = ItsurVinoOscuro
                                    )
                                    Text(
                                        text = doc.nombreArchivo ?: "Pendiente de subir",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ItsurOnSurfaceVariant
                                    )
                                }
                            }
                            StatusChip(status = doc.estado.name)
                        }

                        if (doc.observaciones.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Observaciones previas: ${doc.observaciones}",
                                style = MaterialTheme.typography.bodySmall,
                                color = ItsurOnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (doc.nombreArchivo != null && doc.id.isNotBlank()) {
                            OutlinedButton(
                                onClick = { onPrevisualizarPdf(doc.id, doc.nombreArchivo) },
                                enabled = !uiState.isLoading && !uiState.isDownloadingPdf,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    if (uiState.isDownloadingPdf) "Descargando PDF..." else "Ver / Previsualizar PDF",
                                    color = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onValidarDoc(doc.id, true) },
                                enabled = !uiState.isLoading && doc.estado == EstadoDocumento.SUBIDO,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAprobado, disabledContentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aprobar", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    onObservacionesChange("")
                                    documentoARechazar = doc
                                },
                                enabled = !uiState.isLoading && doc.estado == EstadoDocumento.SUBIDO,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRechazado, disabledContentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Rechazar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Dictamen final del trámite
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dictamen Final de Inscripción",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (uiState.aspirante?.estadoInscripcion == EstadoInscripcion.ACEPTADO) {
                            Text("El trámite está aprobado", style = MaterialTheme.typography.bodySmall)
                        } else {
                            val indicacion = if (uiState.puedeAprobar) {
                                "El trámite está listo para el dictamen final."
                            } else {
                                "Faltan ${uiState.documentosAprobadosFaltantes}. Aprueba todos para habilitar el trámite."
                            }
                            Text(
                                "Aprobados: ${uiState.documentosAprobados} de 4 documentos obligatorios. $indicacion",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = onAprobarAspirante,
                                enabled = uiState.puedeAprobar && !uiState.isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAprobado, disabledContentColor = Color.Black),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().height(46.dp)
                            ) {
                                Text("Aprobar Trámite", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Regresar al Panel", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        documentoARechazar?.let { documento ->
            AlertDialog(
                onDismissRequest = { documentoARechazar = null },
                title = { Text("Rechazar ${documento.tipo.titulo}") },
                text = {
                    OutlinedTextField(
                        value = uiState.observacionesInput,
                        onValueChange = onObservacionesChange,
                        label = { Text("Observaciones obligatorias") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (uiState.observacionesInput.trim().isNotEmpty()) {
                                onValidarDoc(documento.id, false)
                                documentoARechazar = null
                            }
                        },
                        enabled = uiState.observacionesInput.trim().isNotEmpty() && !uiState.isLoading
                    ) { Text("Rechazar documento") }
                },
                dismissButton = {
                    OutlinedButton(onClick = { documentoARechazar = null }) { Text("Cancelar", color = Color.Black) }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminAspiranteDetailPreview() {
    AppTheme {
        AdminAspiranteDetailContent(
            uiState = AdminAspiranteDetailUiState(),
            onNavigateBack = {},
            onObservacionesChange = {},
            onValidarDoc = { _, _ -> },
            onAprobarAspirante = {},
            onPrevisualizarPdf = { _, _ -> }
        )
    }
}
