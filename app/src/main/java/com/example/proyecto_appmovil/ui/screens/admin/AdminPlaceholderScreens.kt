package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.theme.ItsurCiruela
import com.example.proyecto_appmovil.ui.theme.ItsurOffWhite
import com.example.proyecto_appmovil.ui.theme.ItsurRosaPastel

@Composable
fun AdminQuestionsPlaceholderScreen(
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit
) {
    AdminPendingModuleScreen(
        title = "Preguntas y Dudas",
        currentRoute = Screen.AdminQuestions.route,
        icon = Icons.Default.QuestionAnswer,
        heading = "Bandeja de Preguntas y Dudas",
        description = "Este módulo permitirá consultar dudas de aspirantes filtradas por categoría (Fechas / Documentos) y estado (Pendiente / Respondida) en la siguiente fase (F09).",
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout
    )
}

@Composable
fun AdminBroadcastPlaceholderScreen(
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit
) {
    AdminPendingModuleScreen(
        title = "Aviso General",
        currentRoute = Screen.AdminBroadcast.route,
        icon = Icons.Default.Campaign,
        heading = "Emisión de Aviso General",
        description = "Este módulo permitirá enviar avisos masivos a todos los aspirantes registrados en la siguiente fase (F09).",
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout
    )
}

@Composable
private fun AdminPendingModuleScreen(
    title: String,
    currentRoute: String,
    icon: ImageVector,
    heading: String,
    description: String,
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit
) {
    AdminDrawerScaffold(
        title = title,
        currentRoute = currentRoute,
        onNavigateToRoute = onNavigateToRoute,
        onLogout = onLogout
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ItsurOffWhite)
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(ItsurRosaPastel, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = ItsurCiruela,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = heading,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
