package com.example.proyecto_appmovil.navigation

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyecto_appmovil.InscripcionesApp
import com.example.proyecto_appmovil.ui.AppViewModelProvider
import com.example.proyecto_appmovil.ui.screens.admin.AdminAspiranteDetailViewModel
import com.example.proyecto_appmovil.ui.screens.admin.AdminFechasScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminHomeScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminBroadcastScreen
import com.example.proyecto_appmovil.ui.screens.admin.AdminQuestionsScreen
import com.example.proyecto_appmovil.ui.screens.questions.StudentQuestionsScreen
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
    navController: NavHostController = rememberNavController(),
    isAdminExpanded: Boolean = false
) {
    val adminExpanded by rememberUpdatedState(isAdminExpanded)
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
                onNavigateToQuestions = { navController.navigate(Screen.StudentQuestions.route) },
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

        composable(Screen.StudentQuestions.route) {
            StudentQuestionsScreen(onNavigateBack = { navController.popBackStack() })
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
                isExpanded = adminExpanded,
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(Screen.AdminFechas.route) {
            AdminFechasScreen(
                isExpanded = adminExpanded,
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(Screen.AdminQuestions.route) {
            AdminQuestionsScreen(
                isExpanded = adminExpanded,
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(Screen.AdminBroadcast.route) {
            AdminBroadcastScreen(
                isExpanded = adminExpanded,
                onNavigateToRoute = navigateToAdminRoute,
                onLogout = logout
            )
        }

        composable(
            route = Screen.AdminAspiranteDetail.route,
            arguments = listOf(navArgument("aspiranteId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Recupera expedientes abiertos en la ruta anterior del panel.
            val adminHomeEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.AdminHome.route)
            }
            val detailViewModel: AdminAspiranteDetailViewModel = viewModel(
                viewModelStoreOwner = adminHomeEntry,
                factory = AppViewModelProvider.Factory
            )
            LaunchedEffect(backStackEntry) {
                if (navController.currentBackStackEntry == backStackEntry) {
                    detailViewModel.seleccionarAspirante(backStackEntry.arguments?.getString("aspiranteId"))
                    navController.popBackStack()
                }
            }
        }
    }
}
