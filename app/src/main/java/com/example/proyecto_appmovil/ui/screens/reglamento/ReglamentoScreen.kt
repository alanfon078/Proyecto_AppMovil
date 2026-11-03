package com.example.proyecto_appmovil.ui.screens.reglamento

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.components.ErrorBanner
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.components.PrimaryButton
import com.example.proyecto_appmovil.ui.theme.*

@Composable
fun ReglamentoScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReglamentoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()

    ReglamentoContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onToggleTerminos = viewModel::toggleTerminos,
        onSetTieneFirma = viewModel::setTieneFirma,
        onGuardarFirma = viewModel::guardarFirma
    )
}

@Composable
fun ReglamentoContent(
    uiState: ReglamentoUiState,
    onNavigateBack: () -> Unit,
    onToggleTerminos: (Boolean) -> Unit,
    onSetTieneFirma: (Boolean) -> Unit,
    onGuardarFirma: () -> Unit
) {
    val lines = remember { mutableStateListOf<List<Offset>>() }
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Scaffold(
        topBar = {
            ItsurTopBar(
                title = "Reglamento Institucional",
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

            Text(
                text = "Reglamento para Alumnos de Nuevo Ingreso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ItsurVinoOscuro,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visor con extracto del reglamento
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "1. DERECHOS Y OBLIGACIONES:\n" +
                                "El aspirante se compromete a presentar documentación original y fidedigna durante los periodos oficiales establecidos por el Instituto Tecnológico Superior del Sur de Guanajuato (ITSUR).\n\n" +
                                "2. ASISTENCIA Y CURSO PROPEDÉUTICO:\n" +
                                "La asistencia a las sesiones del curso propedéutico y al examen diagnóstico es de carácter indispensable para consolidar el proceso de inscripción.\n\n" +
                                "3. CONDUCTA Y ÉTICA:\n" +
                                "Hacer uso respetuoso de las instalaciones físicas, plataformas digitales y respetar a la comunidad tecnológica en todo momento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = ItsurOnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.errorMessage != null) {
                ErrorBanner(mensaje = uiState.errorMessage ?: "")
            }

            if (uiState.isFirmadoExitosamente) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = StatusAprobadoBg),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StatusAprobado)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "¡Reglamento firmado y aceptado digitalmente!",
                            color = StatusAprobado,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Checkbox de aceptación
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.aceptoTerminos,
                    onCheckedChange = { onToggleTerminos(it) },
                    colors = CheckboxDefaults.colors(checkedColor = ItsurCiruela)
                )
                Text(
                    text = "He leído y acepto los términos y lineamientos del reglamento institucional.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ItsurVinoOscuro
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Canvas interactivo para captura de firma táctil
            Text(
                text = "Traza tu firma en el recuadro:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = ItsurVinoOscuro,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .border(1.5.dp, ItsurCiruela, RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentLine = listOf(offset)
                                    onSetTieneFirma(true)
                                },
                                onDrag = { change, _ ->
                                    currentLine = currentLine + change.position
                                },
                                onDragEnd = {
                                    lines.add(currentLine)
                                    currentLine = emptyList()
                                }
                            )
                        }
                ) {
                    val stroke = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )

                    lines.forEach { line ->
                        if (line.size > 1) {
                            val path = Path().apply {
                                moveTo(line.first().x, line.first().y)
                                for (i in 1 until line.size) {
                                    lineTo(line[i].x, line[i].y)
                                }
                            }
                            drawPath(path, color = ItsurVinoOscuro, style = stroke)
                        }
                    }

                    if (currentLine.size > 1) {
                        val path = Path().apply {
                            moveTo(currentLine.first().x, currentLine.first().y)
                            for (i in 1 until currentLine.size) {
                                lineTo(currentLine[i].x, currentLine[i].y)
                            }
                        }
                        drawPath(path, color = ItsurVinoOscuro, style = stroke)
                    }
                }

                IconButton(
                    onClick = {
                        lines.clear()
                        currentLine = emptyList()
                        onSetTieneFirma(false)
                    },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar firma",
                        tint = ItsurCiruela
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = "Guardar y Aceptar Firma",
                isLoading = uiState.isLoading,
                onClick = onGuardarFirma
            )

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
                Text("Regresar al Menú Principal", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReglamentoScreenPreview() {
    AppTheme {
        ReglamentoContent(
            uiState = ReglamentoUiState(
                aceptoTerminos = true,
                tieneFirma = true
            ),
            onNavigateBack = {},
            onToggleTerminos = {},
            onSetTieneFirma = {},
            onGuardarFirma = {}
        )
    }
}
