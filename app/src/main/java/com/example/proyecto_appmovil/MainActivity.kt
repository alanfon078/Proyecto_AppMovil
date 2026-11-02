package com.example.proyecto_appmovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.proyecto_appmovil.navigation.AppNavHost
import com.example.proyecto_appmovil.ui.theme.Color50
import com.example.proyecto_appmovil.ui.theme.Proyecto_AppMovilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Proyecto_AppMovilTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color50
                ) {
                    AppNavHost()
                }
            }
        }
    }
}
