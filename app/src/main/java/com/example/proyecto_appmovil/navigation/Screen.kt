package com.example.proyecto_appmovil.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Documents : Screen("documents")
    object Status : Screen("status")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object CredentialPhoto : Screen("credential_photo")
    object Payment : Screen("payment")
}
