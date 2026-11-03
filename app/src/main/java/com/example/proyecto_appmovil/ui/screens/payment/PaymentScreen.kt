package com.example.proyecto_appmovil.ui.screens.payment

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payment
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
import com.example.proyecto_appmovil.model.ComprobantePago
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.*
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun PaymentScreen(
    onNavigateBack: () -> Unit,
    viewModel: PaymentViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "voucher.pdf"
            viewModel.onComprobanteAdjunto(name)
        }
    }

    PaymentContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onFolioChange = viewModel::onFolioChange,
        onMontoChange = viewModel::onMontoChange,
        onFechaChange = viewModel::onFechaChange,
        onAdjuntarClick = { filePicker.launch("image/*,application/pdf") },
        onRegistrarPago = viewModel::registrarPago
    )
}

@Composable
fun PaymentContent(
    uiState: PaymentUiState,
    onNavigateBack: () -> Unit,
    onFolioChange: (String) -> Unit,
    onMontoChange: (String) -> Unit,
    onFechaChange: (String) -> Unit,
    onAdjuntarClick: () -> Unit,
    onRegistrarPago: () -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Comprobante de Pago",
                onNavigateBack = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Ficha de Pago Institucional
            Card(
                colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Ficha Institucional ITSUR",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        StatusChip(status = if (uiState.comprobante?.validado == true) "ACEPTADO" else "EN_REVISION")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = ItsurCiruela,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Banco: BBVA • Convenio CIE: 1849201",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ItsurVinoOscuro,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Referencia: ITSUR-2026-0042\nCuota Semestral: $2,850.00 MXN",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ItsurOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.mensajeExito != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.mensajeExito ?: "",
                        color = StatusAprobado,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (uiState.mensajeError != null) {
                ErrorBanner(mensaje = uiState.mensajeError ?: "")
            }

            // Formulario de reporte de pago
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Reportar Comprobante de Pago",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ItsurVinoOscuro
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ItsurTextField(
                        value = uiState.folioInput,
                        onValueChange = onFolioChange,
                        label = "Número de Folio / Referencia Bancaria",
                        leadingIcon = Icons.Default.Payment
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ItsurTextField(
                        value = uiState.montoInput,
                        onValueChange = onMontoChange,
                        label = "Monto Pagado ($ MXN)"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ItsurTextField(
                        value = uiState.fechaInput,
                        onValueChange = onFechaChange,
                        label = "Fecha de Operación"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = onAdjuntarClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.AttachFile, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (uiState.comprobanteAdjuntoNombre != null) "Adjunto: ${uiState.comprobanteAdjuntoNombre}" else "Adjuntar Foto o PDF del Voucher",
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "Registrar Comprobante",
                        isLoading = uiState.isLoading,
                        onClick = onRegistrarPago
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PaymentScreenPreview() {
    AppTheme {
        PaymentContent(
            uiState = PaymentUiState(
                comprobante = ComprobantePago(
                    folio = "BBVA-849201",
                    monto = 2850.0,
                    validado = false
                ),
                folioInput = "BBVA-849201",
                montoInput = "2850.00",
                fechaInput = "15/07/2026"
            ),
            onNavigateBack = {},
            onFolioChange = {},
            onMontoChange = {},
            onFechaChange = {},
            onAdjuntarClick = {},
            onRegistrarPago = {}
        )
    }
}
