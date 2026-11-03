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
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTextField
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
        onDictaminar = viewModel::dictaminarAspirante
    )
}

@Composable
fun AdminAspiranteDetailContent(
    uiState: AdminAspiranteDetailUiState,
    onNavigateBack: () -> Unit,
    onObservacionesChange: (String) -> Unit,
    onValidarDoc: (String, Boolean) -> Unit,
    onDictaminar: (Boolean) -> Unit
) {
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
                            StatusChip(status = asp?.estadoInscripcion?.name ?: "EN_REVISION")
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = ItsurVinoOscuro.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(10.dp))

                        DatoItem(label = "Carrera de Selección", valor = asp?.carrera ?: "")
                        DatoItem(label = "CURP", valor = asp?.curp ?: "")
                        DatoItem(label = "Correo Institucional", valor = asp?.email ?: "")
                        DatoItem(label = "Teléfono", valor = asp?.telefono ?: "")
                        DatoItem(label = "Promedio Bachillerato", valor = "${asp?.promedioBachillerato ?: 0.0}")
                        DatoItem(label = "Dirección", valor = asp?.direccion ?: "")
                    }
                }
            }

            item {
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

            items(uiState.documentos, key = { it.id }) { doc ->
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onValidarDoc(doc.id, true) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAprobado),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Aprobar", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { onValidarDoc(doc.id, false) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRechazado),
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
                        ItsurTextField(
                            value = uiState.observacionesInput,
                            onValueChange = onObservacionesChange,
                            label = "Observaciones oficiales (obligatorias para rechazo)"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { onDictaminar(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusAprobado),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text("Aprobar Trámite", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = { onDictaminar(false) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusRechazado),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                            ) {
                                Text("Rechazar Trámite", fontWeight = FontWeight.Bold)
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
                    Text("Regresar al Panel", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AdminAspiranteDetailPreview() {
    AppTheme {
        AdminAspiranteDetailContent(
            uiState = AdminAspiranteDetailUiState(
                aspirante = Aspirante(
                    nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
                    folioAspirante = "ITSUR-2026-0042",
                    carrera = "Ing. Sistemas Computacionales",
                    curp = "OOAD030515HGTRRN01",
                    email = "aspirante@itsur.edu.mx",
                    telefono = "4451234567",
                    promedioBachillerato = 9.4,
                    direccion = "Uriangato, Gto.",
                    estadoInscripcion = EstadoInscripcion.EN_REVISION
                ),
                documentos = listOf(
                    Documento("1", TipoDocumento.ACTA_NACIMIENTO, "acta_nacimiento.pdf", estado = EstadoDocumento.VALIDADO),
                    Documento("2", TipoDocumento.CURP, "curp_certificada.pdf", estado = EstadoDocumento.SUBIDO)
                )
            ),
            onNavigateBack = {},
            onObservacionesChange = {},
            onValidarDoc = { _, _ -> },
            onDictaminar = {}
        )
    }
}
