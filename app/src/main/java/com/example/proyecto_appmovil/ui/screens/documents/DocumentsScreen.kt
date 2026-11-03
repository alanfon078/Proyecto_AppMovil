package com.example.proyecto_appmovil.ui.screens.documents

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.proyecto_appmovil.model.Documento
import com.example.proyecto_appmovil.model.EstadoDocumento
import com.example.proyecto_appmovil.model.TipoDocumento
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun DocumentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: DocumentsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "documento.pdf"
            viewModel.onArchivoSeleccionado(
                uriString = uri.toString(),
                nombre = if (fileName.endsWith(".pdf", ignoreCase = true)) fileName else "$fileName.pdf",
                tamano = 1024 * 1024 * 2 // ~2MB simulado
            )
        }
    }

    DocumentsContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onSubirClick = { tipo ->
            viewModel.prepararSubida(tipo)
            filePickerLauncher.launch(arrayOf("application/pdf"))
        }
    )
}

@Composable
fun DocumentsContent(
    uiState: DocumentsUiState,
    onNavigateBack: () -> Unit,
    onSubirClick: (TipoDocumento) -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Carga de Documentos",
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

                // Card explicativa
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = ItsurVinoOscuro,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Por favor sube tus documentos oficiales legibles en formato PDF (peso máx. 5 MB). Serán validados por Servicios Escolares.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ItsurVinoOscuro
                        )
                    }
                }

                if (uiState.mensajeError != null) {
                    ErrorBanner(mensaje = uiState.mensajeError ?: "")
                }

                if (uiState.mensajeExito != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusAprobado
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.mensajeExito ?: "",
                                color = StatusAprobado,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            items(uiState.documentos, key = { it.id }) { doc ->
                DocumentoItemCard(
                    documento = doc,
                    onSubirClick = { onSubirClick(doc.tipo) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
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

@Composable
fun DocumentoItemCard(
    documento: Documento,
    onSubirClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        text = documento.tipo.titulo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )
                    Text(
                        text = documento.tipo.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = ItsurOnSurfaceVariant
                    )
                }
                StatusChip(status = documento.estado.name)
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (documento.nombreArchivo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ItsurRosaSuave, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        tint = ItsurCiruela,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = documento.nombreArchivo,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        val pesoStr = if (documento.tamanoBytes > 0) "${documento.tamanoBytes / 1024} KB" else "2.1 MB"
                        Text(
                            text = "Versión: v${documento.version}.0 • Tamaño: $pesoStr • Cargado: ${documento.fechaSubida ?: "Reciente"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = ItsurOnSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (documento.observaciones.isNotBlank()) {
                Text(
                    text = "Nota del revisor: ${documento.observaciones}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ItsurOnSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Button(
                onClick = onSubirClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (documento.nombreArchivo != null) ItsurRosaPastel else ItsurCiruela,
                    contentColor = if (documento.nombreArchivo != null) ItsurVinoOscuro else Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (documento.nombreArchivo != null) Icons.Default.Refresh else Icons.Default.UploadFile,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (documento.nombreArchivo != null) "Reemplazar Documento" else "Subir Documento (PDF)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocumentsScreenPreview() {
    AppTheme {
        DocumentsContent(
            uiState = DocumentsUiState(
                documentos = listOf(
                    Documento(
                        id = "1",
                        tipo = TipoDocumento.ACTA_NACIMIENTO,
                        nombreArchivo = "acta_nacimiento_valida.pdf",
                        tamanoBytes = 2048 * 1024,
                        fechaSubida = "Hoy",
                        estado = EstadoDocumento.VALIDADO,
                        version = 1
                    ),
                    Documento(
                        id = "2",
                        tipo = TipoDocumento.CURP,
                        nombreArchivo = null,
                        estado = EstadoDocumento.PENDIENTE,
                        version = 1
                    )
                )
            ),
            onNavigateBack = {},
            onSubirClick = {}
        )
    }
}
