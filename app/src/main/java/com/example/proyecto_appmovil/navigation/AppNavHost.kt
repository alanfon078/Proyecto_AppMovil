package com.example.proyecto_appmovil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_appmovil.ui.screens.admin.AdminAspiranteDetailScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminFechasScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminHomeScreen
import com.example.proyecto_appmovil.ui.screens.auth.LoginScreen
import com.example.proyecto_appmovil.ui.screens.auth.RegisterScreen
import com.example.proyecto_appmovil.ui.screens.documents.DocumentsScreen
import com.example.proyecto_appmovil.ui.screens.home.HomeScreen
import com.example.proyecto_appmovil.ui.screens.notifications.NotificationsScreen
import com.example.proyecto_appmovil.ui.screens.payment.PaymentScreen
import com.example.proyecto_appmovil.ui.screens.profile.ProfileScreen
import com.example.proyecto_appmovil.ui.screens.status.StatusScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val logout = {
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
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
                        popUpTo(Screen.Register.route) { inclusive = true }
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
                onNavigateToPayment = { navController.navigate(Screen.Payment.route) },
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

        composable(Screen.Payment.route) {
            PaymentScreen(onNavigateBack = { navController.popBackStack() })
        }

        // ── MÓDULO ADMIN (CONTROL ESCOLAR) ────────────────────────────────
        composable(Screen.AdminHome.route) {
            AdminHomeScreen(
                onNavigateToFechas = { navController.navigate(Screen.AdminFechas.route) },
                onNavigateToAspiranteDetail = { aspiranteId ->
                    navController.navigate(Screen.AdminAspiranteDetail.createRoute(aspiranteId))
                },
                onLogout = logout
            )
        }

        composable(Screen.AdminFechas.route) {
            AdminFechasScreen(
                onNavigateBack = { navController.popBackStack() }
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
