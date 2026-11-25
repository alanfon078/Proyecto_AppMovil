package com.example.proyecto_appmovil.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.OptionCard
import com.example.proyecto_appmovil.ui.components.StatusChip
import com.example.proyecto_appmovil.ui.theme.*
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.proyecto_appmovil.work.NotificacionesWorkManager
import androidx.compose.ui.platform.LocalContext

@Composable
fun HomeScreen(
    onNavigateToDocuments: () -> Unit,
    onNavigateToStatus: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToQuestions: () -> Unit,
    onLogout: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.cargarDatos()
    }
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        NotificacionesWorkManager.schedulePeriodic(context)
        NotificacionesWorkManager.syncNow(context)
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) NotificacionesWorkManager.syncNow(context)
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    HomeContent(
        uiState = uiState,
        onNavigateToDocuments = onNavigateToDocuments,
        onNavigateToStatus = onNavigateToStatus,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToNotifications = onNavigateToNotifications,
        onNavigateToQuestions = onNavigateToQuestions,
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
    onNavigateToQuestions: () -> Unit,
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

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            uiState.mensajeError?.let { ErrorBanner(mensaje = it) }

            uiState.aspirante?.let { aspirante ->
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
                            StatusChip(status = aspirante.estadoInscripcion.name)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = aspirante.nombreCompleto,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = ItsurVinoOscuro
                        )

                        Text(
                            text = "Folio: ${aspirante.folioAspirante} • ${aspirante.carrera}",
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
                                color = ItsurOnSurface
                            )
                        }
                    }
                }
            }

            OptionCard(
                titulo = "Carga de Documentos",
                descripcion = "Sube tus archivos PDF (Acta, CURP, Certificado y Comprobante de Domicilio)",
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
                titulo = "Preguntas y Dudas",
                descripcion = "Consulta dudas sobre Fechas y Documentos",
                icono = Icons.Default.QuestionAnswer,
                badgeCount = uiState.respuestasNoLeidasCount,
                onClick = onNavigateToQuestions
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    AppTheme {
        HomeContent(
            uiState = HomeUiState(),
            onNavigateToDocuments = {},
            onNavigateToStatus = {},
            onNavigateToProfile = {},
            onNavigateToNotifications = {},
            onNavigateToQuestions = {},
            onLogout = {}
        )
    }
}
