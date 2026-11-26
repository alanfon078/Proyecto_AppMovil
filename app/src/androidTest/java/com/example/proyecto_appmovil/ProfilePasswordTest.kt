package com.example.proyecto_appmovil

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.proyecto_appmovil.ui.screens.profile.ProfileContent
import com.example.proyecto_appmovil.ui.screens.profile.ProfileUiState
import com.example.proyecto_appmovil.ui.theme.AppTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfilePasswordTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun siguienteEnfocaYMuestraLaConfirmacionYLaContrasenaNueva() {
        compose.setContent {
            AppTheme {
                ProfileContent(
                    uiState = ProfileUiState(),
                    onNavigateBack = {},
                    onToggleEdit = {},
                    onTelefonoChange = {},
                    onDireccionChange = {},
                    onEstadoChange = {},
                    onMunicipioChange = {},
                    onGuardarCambios = {},
                    onPasswordActualChange = {},
                    onConfirmacionPasswordActualChange = {},
                    onPasswordNuevaChange = {},
                    onCambiarPassword = {}
                )
            }
        }

        compose.onNodeWithText("Contraseña Actual")
            .performScrollTo()
            .performClick()
            .performImeAction()

        compose.onNodeWithText("Confirmar Contraseña Actual")
            .assertIsFocused()
            .assertIsDisplayed()
            .performImeAction()

        compose.onNodeWithText("Nueva Contraseña (mín. 8 caracteres)")
            .assertIsFocused()
            .assertIsDisplayed()
    }
}
