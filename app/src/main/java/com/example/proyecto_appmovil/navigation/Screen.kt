package com.example.proyecto_appmovil.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Documents : Screen("documents")
    object Status : Screen("status")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object StudentQuestions : Screen("student_questions")

    // Módulos para rol Escolar / Administrador
    object AdminHome : Screen("admin_home")
    object AdminFechas : Screen("admin_fechas")
    object AdminQuestions : Screen("admin_questions")
    object AdminBroadcast : Screen("admin_broadcast")
    object AdminAspiranteDetail : Screen("admin_aspirante/{aspiranteId}") {
        fun createRoute(aspiranteId: String): String = "admin_aspirante/$aspiranteId"
    }
}
