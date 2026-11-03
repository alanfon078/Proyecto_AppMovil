package com.example.proyecto_appmovil.ui.screens.photo

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.PrimaryButton
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun CredentialPhotoScreen(
    onNavigateBack: () -> Unit,
    viewModel: CredentialPhotoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.onPhotoSelected(uri.toString())
        }
    }

    CredentialPhotoContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onElegirFoto = { galleryLauncher.launch("image/*") },
        onGuardarFoto = viewModel::guardarFoto
    )
}

@Composable
fun CredentialPhotoContent(
    uiState: CredentialPhotoUiState,
    onNavigateBack: () -> Unit,
    onElegirFoto: () -> Unit,
    onGuardarFoto: () -> Unit
) {
    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Foto para Credencial",
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

            // Instrucciones de la foto
            Card(
                colors = CardDefaults.cardColors(containerColor = ItsurRosaPastel),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = ItsurVinoOscuro,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Requisitos de la Fotografía",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• Fondo completamente blanco y liso.\n• Rostro centrado y de frente.\n• Sin gorras, lentes o accesorios.\n• Iluminación clara y nítida.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ItsurVinoOscuro
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Marco de previsualización 3:4
            Box(
                modifier = Modifier
                    .width(180.dp)
                    .height(240.dp)
                    .background(Color.White, RoundedCornerShape(16.dp))
                    .border(2.dp, ItsurCiruela, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.photoUriString != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = StatusAprobado,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Foto cargada",
                            fontWeight = FontWeight.Bold,
                            color = ItsurVinoOscuro
                        )
                        Text(
                            text = "Formato 3:4 listo",
                            style = MaterialTheme.typography.labelSmall,
                            color = ItsurOnSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = ItsurOutline,
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vista previa (3:4)",
                            style = MaterialTheme.typography.bodySmall,
                            color = ItsurOutline,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isSuccess) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "¡Fotografía guardada con éxito para tu credencial escolar!",
                        color = StatusAprobado,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = onElegirFoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ItsurRosaPastel,
                    contentColor = ItsurVinoOscuro
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.photoUriString != null) "Cambiar Fotografía" else "Elegir de Galería o Tomar Foto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(
                text = "Guardar Fotografía",
                enabled = uiState.photoUriString != null,
                isLoading = uiState.isUploading,
                onClick = onGuardarFoto
            )

            Spacer(modifier = Modifier.height(20.dp))

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
fun CredentialPhotoScreenPreview() {
    AppTheme {
        CredentialPhotoContent(
            uiState = CredentialPhotoUiState(),
            onNavigateBack = {},
            onElegirFoto = {},
            onGuardarFoto = {}
        )
    }
}
