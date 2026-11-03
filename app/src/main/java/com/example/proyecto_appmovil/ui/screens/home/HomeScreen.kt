package com.example.proyecto_appmovil.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.model.Aspirante
import com.example.proyecto_appmovil.model.EstadoInscripcion
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.OptionCard
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToDocuments: () -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPhoto: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToReglamento: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }

    HomeContent(
        uiState = uiState,
        onNavigateToDocuments = onNavigateToDocuments,
        onNavigateToStatus = onNavigateToStatus,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToPhoto = onNavigateToPhoto,
        onNavigateToPayment = onNavigateToPayment,
        onNavigateToReglamento = onNavigateToReglamento,
        onLogout = onLogout
    )
}

@Composable
fun HomeContent(
    uiState: HomeUiState,
    onNavigateToDocuments: () -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToPhoto: () -> Unit,
    onNavigateToPayment: () -> Unit,
    onNavigateToReglamento: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Menú Principal",
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
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de bienvenida del aspirante
            Card(
                colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¡Bienvenido, Aspirante!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        StatusChip(status = uiState.aspirante?.estadoInscripcion?.name ?: "EN_REVISION")
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = uiState.aspirante?.nombreCompleto ?: "Aspirante ITSUR",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = ItsurVinoOscuro
                    )

                    Text(
                        text = "Folio: ${uiState.aspirante?.folioAspirante ?: "ITSUR-2026-0042"} • ${uiState.aspirante?.carrera ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ItsurOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(color = ItsurVinoOscuro.copy(alpha = 0.2f))

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Avance de Documentos:",
                            style = MaterialTheme.typography.bodySmall,
                            color = ItsurOnSurfaceVariant
                        )
                        Text(
                            text = "${uiState.documentosSubidosCount} de ${uiState.totalDocumentosCount} completados",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = ItsurCiruela
                        )
                    }
                }
            }

            // Opciones del Menú (6 tarjetas principales + reglamento)
            OptionCard(
                titulo = "Carga de Documentos",
                descripcion = "Sube tus archivos PDF (Acta, CURP, Certificado)",
                icono = Icons.Default.CloudUpload,
                badgeCount = (uiState.totalDocumentosCount - uiState.documentosSubidosCount).coerceAtLeast(0),
                onClick = onNavigateToDocuments
            )

            Spacer(modifier = Modifier.height(12.dp))

            OptionCard(
                titulo = "Estatus de Inscripción",
                descripcion = "Consulta el avance en tiempo real de tu trámite",
                icono = Icons.Default.Timeline,
                onClick = onNavigateToStatus
            )

            Spacer(modifier = Modifier.height(12.dp))

            OptionCard(
                titulo = "Datos Personales",
                descripcion = "Revisa y actualiza tu información de contacto",
                icono = Icons.Default.AccountCircle,
                onClick = onNavigateToProfile
            )

            Spacer(modifier = Modifier.height(12.dp))

            OptionCard(
                titulo = "Notificaciones y Fechas",
                descripcion = "Avisos importantes y fechas límite del proceso",
                icono = Icons.Default.Notifications,
                badgeCount = uiState.notificacionesNoLeidasCount,
                onClick = onNavigateToNotifications
            )

            Spacer(modifier = Modifier.height(12.dp))

            OptionCard(
                titulo = "Foto para Credencial",
                descripcion = "Fotografía digital con fondo blanco para la credencial",
                icono = Icons.Default.CameraAlt,
                onClick = onNavigateToPhoto
            )

            Spacer(modifier = Modifier.height(12.dp))

            OptionCard(
                titulo = "Comprobante de Pago",
                descripcion = "Ficha de pago y reporte del comprobante bancario",
                icono = Icons.Default.Payment,
                onClick = onNavigateToPayment
            )

            Spacer(modifier = Modifier.height(12.dp))

            OptionCard(
                titulo = "Reglamento Institucional",
                descripcion = "Lectura de normas y captura de firma electrónica",
                icono = Icons.Default.Draw,
                onClick = onNavigateToReglamento
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onLogout,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ItsurCiruela),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Cerrar Sesión", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeContent(
            uiState = HomeUiState(
                aspirante = Aspirante(
                    nombreCompleto = "Dafne Guadalupe Orozco Aguirre",
                    folioAspirante = "ITSUR-2026-0042",
                    carrera = "Ing. Sistemas Computacionales",
                    estadoInscripcion = EstadoInscripcion.EN_REVISION
                ),
                totalDocumentosCount = 7,
                documentosSubidosCount = 4,
                notificacionesNoLeidasCount = 2
            ),
            onNavigateToDocuments = {},
            onNavigateToStatus = {},
            onNavigateToProfile = {},
            onNavigateToNotifications = {},
            onNavigateToPhoto = {},
            onNavigateToPayment = {},
            onNavigateToReglamento = {},
            onLogout = {}
        )
    }
}
