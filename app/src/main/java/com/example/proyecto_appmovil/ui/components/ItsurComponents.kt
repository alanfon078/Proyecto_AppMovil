package com.example.proyecto_appmovil.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.proyecto_appmovil.ui.theme.*

/**
 * Barra superior institucional ITSUR con color ciruela/vino,
 * ícono de regreso opcional y botón de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItsurTopBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    onLogout: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Regresar",
                        tint = Color.White
                    )
                }
            }
        },
        actions = {
            if (onLogout != null) {
                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Cerrar sesión",
                        tint = Color.White
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = ItsurCiruela,
            titleContentColor = Color.White
        )
    )
}

/**
 * Campo de texto estilizado ITSUR con leading icon, visualización de password,
 * validación con mensaje de error en rojo y soporte de teclado IME.
 */
@Composable
fun ItsurTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val isError = errorMessage != null && errorMessage.isNotBlank()

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else ItsurCiruela
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = image,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = ItsurCiruela
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            enabled = enabled,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ItsurCiruela,
                focusedLabelColor = ItsurCiruela,
                cursorColor = ItsurCiruela,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (isError) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}

/**
 * Botón principal ciruela con estado de carga (CircularProgressIndicator) y deshabilitado automático.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = ItsurCiruela,
            contentColor = Color.White,
            disabledContainerColor = ItsurCiruela.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Text(text = text, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Tarjeta de opción para el Menú Principal con ícono temático, título en negritas,
 * subtítulo descriptivo, badge de conteo opcional y chevron '>'
 */
@Composable
fun OptionCard(
    titulo: String,
    descripcion: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = ItsurRosaSuave
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = ItsurRosaPastel, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = ItsurVinoOscuro,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ItsurVinoOscuro
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = ItsurOnSurfaceVariant
                )
            }

            if (badgeCount > 0) {
                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .background(color = ItsurCiruela, shape = CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$badgeCount",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = ItsurCiruela
            )
        }
    }
}

/**
 * Pastilla de estado institucional (En captura, En revisión, Con observaciones, Aceptado, Rechazado)
 */
@Composable
fun StatusChip(
    status: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when (status.uppercase()) {
        "ACEPTADO", "APROBADO", "VALIDADO" -> Triple(StatusAprobadoBg, StatusAprobado, "Aceptado")
        "EN_REVISION", "SUBIDO" -> Triple(StatusPendienteBg, StatusPendiente, "En Revisión")
        "CON_OBSERVACIONES", "OBSERVADO" -> Triple(StatusObservacionesBg, StatusObservaciones, "Con Observaciones")
        "RECHAZADO" -> Triple(StatusRechazadoBg, StatusRechazado, "Rechazado")
        else -> Triple(Color(0xFFE0E0E0), Color(0xFF616161), "En Captura / Pendiente")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Estado vacío para cuando una lista no tiene elementos.
 */
@Composable
fun EmptyState(
    mensaje: String,
    icono: ImageVector = Icons.Default.Info,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = ItsurOutline,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = mensaje,
            style = MaterialTheme.typography.bodyMedium,
            color = ItsurOnSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Banner de error visual.
 */
@Composable
fun ErrorBanner(
    mensaje: String,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
