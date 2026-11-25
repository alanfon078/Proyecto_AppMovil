package com.example.proyecto_appmovil.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_appmovil.InscripcionesApp
import com.example.proyecto_appmovil.ui.screens.admin.AdminAspiranteDetailScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminBroadcastPlaceholderScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminFechasScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminHomeScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminQuestionsPlaceholderScreen
import com.example.proyecto_appmovil.ui.screens.auth.LoginScreen
import com.example.proyecto_appmovil.ui.screens.auth.RegisterScreen
import com.example.proyecto_appmovil.ui.screens.documents.DocumentsScreen
import com.example.proyecto_appmovil.ui.screens.home.HomeScreen
import com.example.proyecto_appmovil.ui.screens.notifications.NotificationsScreen
import com.example.proyecto_appmovil.ui.screens.profile.ProfileScreen
import com.example.proyecto_appmovil.ui.screens.status.StatusScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val app = LocalContext.current.applicationContext as InscripcionesApp
    val scope = rememberCoroutineScope()
    var cerrandoSesion by remember { mutableStateOf(false) }
    val logout: () -> Unit = {
        if (!cerrandoSesion) {
            cerrandoSesion = true
            scope.launch {
                try {
                    val resultado = app.container.inscripcionesRepository.logout()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.id)
                        launchSingleTop = true
                    }
                    resultado.onFailure {
                        Toast.makeText(
                            app,
                            "La sesión local se cerró. No se pudo confirmar el cierre en el servidor.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } finally {
                    cerrandoSesion = false
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {

        // ── AUTENTICACIÓN ─────────────────────────────────────────────────
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToAdminHome = {
                    navController.navigate(Screen.AdminHome.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // ── MENÚ PRINCIPAL (ASPIRANTE) ────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDocuments = { navController.navigate(Screen.Documents.route) },
                onNavigateToStatus = { navController.navigate(Screen.Status.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onLogout = logout
            )
        }

        // ── PANTALLAS DEL ASPIRANTE ───────────────────────────────────────
        composable(Screen.Documents.route) {
            DocumentsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Status.route) {
            StatusScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDocuments = { navController.navigate(Screen.Documents.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── MÓDULO ADMIN (CONTROL ESCOLAR) ────────────────────────────────
        val navigateToAdminRoute: (String) -> Unit = { route ->
            navController.navigate(route) {
                popUpTo(Screen.AdminHome.route) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }

        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                onNavigateToRoute = navigateToAdminRoute,
                onNavigateToAspiranteDetail = { aspiranteId ->
                    navController.navigate(Screen.AdminAspiranteDetail.createRoute(aspiranteId))
                },
                onLogout = logout
            )
        }

        composable(Screen.AdminFechas.route) {
            AdminFechasScreen(
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(Screen.AdminQuestions.route) {
            AdminQuestionsPlaceholderScreen(
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(Screen.AdminBroadcast.route) {
            AdminBroadcastPlaceholderScreen(
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(
            route = Screen.AdminAspiranteDetail.route,
            arguments = listOf(navArgument("aspiranteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val aspiranteId = backStackEntry.arguments?.getString("aspiranteId") ?: ""
            AdminAspiranteDetailScreen(
                aspiranteId = aspiranteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
