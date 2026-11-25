package com.example.proyecto_appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.proyecto_appmovil.navigation.AppNavHost
import com.example.proyecto_appmovil.navigation.Screen
import com.example.proyecto_appmovil.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val systemBars = WindowCompat.getInsetsController(window, window.decorView)
        setContent {
            val navController = rememberNavController()
            val entry by navController.currentBackStackEntryAsState()
            SideEffect {
                systemBars.isAppearanceLightStatusBars = entry == null || entry?.destination?.route == Screen.Login.route
                systemBars.isAppearanceLightNavigationBars = true
            }
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavHost(navController)
                }
            }
        }
    }
}
