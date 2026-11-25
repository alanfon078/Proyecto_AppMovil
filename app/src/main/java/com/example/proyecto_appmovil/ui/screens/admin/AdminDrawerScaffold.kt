package com.example.proyecto_appmovil.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.proyecto_appmovil.R
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.components.ItsurTopBar
import com.example.proyecto_appmovil.ui.theme.ItsurCiruela
import com.example.proyecto_appmovil.ui.theme.ItsurOffWhite
import com.example.proyecto_appmovil.ui.theme.ItsurRosaPastel
import kotlinx.coroutines.launch

private data class AdminDrawerItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val adminDrawerItems = listOf(
    AdminDrawerItem(Screen.AdminHome.route, "Panel de Control", Icons.Default.AdminPanelSettings),
    AdminDrawerItem(Screen.AdminFechas.route, "Fechas Límite", Icons.Default.CalendarMonth),
    AdminDrawerItem(Screen.AdminQuestions.route, "Preguntas y Dudas", Icons.Default.QuestionAnswer),
    AdminDrawerItem(Screen.AdminBroadcast.route, "Aviso General", Icons.Default.Campaign)
)

@Composable
fun AdminDrawerScaffold(
    title: String,
    currentRoute: String,
    onNavigateToRoute: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val itemColors = NavigationDrawerItemDefaults.colors(
        selectedContainerColor = ItsurRosaPastel,
        selectedIconColor = Color.Black,
        selectedTextColor = Color.Black,
        unselectedContainerColor = Color.Transparent,
        unselectedIconColor = Color.Black,
        unselectedTextColor = Color.Black
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerContentColor = Color.Black
            ) {
                // Cabecera institucional ITSUR
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ItsurCiruela)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.logo_itsur),
                            contentDescription = "Logo ITSUR",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(6.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "ITSUR",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Control Escolar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                adminDrawerItems.forEach { item ->
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                if (currentRoute != item.route) {
                                    onNavigateToRoute(item.route)
                                }
                            }
                        },
                        icon = {
                            Icon(imageVector = item.icon, contentDescription = null)
                        },
                        colors = itemColors,
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                            .height(54.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = {
                        Text(
                            text = "Cerrar Sesión",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    selected = false,
                    onClick = {
                        scope.launch {
                            drawerState.close()
                            onLogout()
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null
                        )
                    },
                    colors = itemColors,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .height(54.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    ) {
        Scaffold(
            containerColor = ItsurOffWhite,
            topBar = {
                ItsurTopBar(
                    title = title,
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                )
            },
            content = content
        )
    }
}
