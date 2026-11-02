package com.example.proyecto_appmovil.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_appmovil.ui.screens.auth.LoginScreen
import com.example.proyecto_appmovil.ui.screens.auth.RegisterScreen
import com.example.proyecto_appmovil.ui.screens.documents.DocumentsScreen
import com.example.proyecto_appmovil.ui.screens.home.HomeScreen
import com.example.proyecto_appmovil.ui.screens.notifications.NotificationsScreen
import com.example.proyecto_appmovil.ui.screens.payment.PaymentScreen
import com.example.proyecto_appmovil.ui.screens.photo.CredentialPhotoScreen
import com.example.proyecto_appmovil.ui.screens.profile.ProfileScreen
import com.example.proyecto_appmovil.ui.screens.status.StatusScreen

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
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

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToDocuments = { navController.navigate(Screen.Documents.route) },
                onNavigateToStatus = { navController.navigate(Screen.Status.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                onNavigateToPhoto = { navController.navigate(Screen.CredentialPhoto.route) },
                onNavigateToPayment = { navController.navigate(Screen.Payment.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Documents.route) {
            DocumentsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Status.route) {
            StatusScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Notifications.route) {
            NotificationsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.CredentialPhoto.route) {
            CredentialPhotoScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Screen.Payment.route) {
            PaymentScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
