package com.example.proyecto_appmovil.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Paleta Oficial Seleccionada ITSUR (Azules y Grises)
// ACD0F2, A7C7E4, A2BDD7, 9EB4CA, 99ABBE, 94A3B1, 8A9198, 85898C, 808080
// ============================================================================
val ColorAzulCielo = Color(0xFFACD0F2)    // ACDOF2 (ACD0F2) - Azul cielo suave para contenedores principales
val ColorAzulPastel = Color(0xFFA7C7E4)   // A7C7E4 - Azul pastel hielo para tarjetas secundarias
val ColorAzulAcero = Color(0xFFA2BDD7)    // A2BDD7 - Azul acero claro
val ColorGrisAzulado1 = Color(0xFF9EB4CA)// 9EB4CA - Gris azulado suave
val ColorGrisAzulado2 = Color(0xFF99ABBE)// 99ABBE - Gris azulado medio
val ColorPizarra = Color(0xFF94A3B1)     // 94A3B1 - Gris pizarra
val ColorGrisFrio = Color(0xFF8A9198)    // 8A9198 - Gris frío para bordes
val ColorGrisMedio = Color(0xFF85898C)   // 85898C - Gris para texto secundario
val ColorGrisNeutro = Color(0xFF808080)  // 808080 - Gris neutro equilibrado

// Mapeo a Identidad Institucional (reemplaza rosas y morados por azules y grises)
val ItsurCiruela = Color(0xFF1E3A5F)        // Primario: Azul marino profundo elegante (TopBar, botones principales)
val ItsurVinoOscuro = Color.Black     // Títulos principales y texto de máximo contraste
val ItsurRosaPastel = ColorAzulCielo        // Banner de bienvenida, contenedores destacados -> #ACD0F2
val ItsurRosaSuave = ColorAzulPastel        // Tarjetas de opción y contenedores secundarios -> #A7C7E4
val ItsurOffWhite = Color(0xFFF4F7FB)       // Fondo general de las pantallas (blanco frío limpio)
val ItsurSurfaceVariant = Color(0xFFE2EAF2) // Variantes de superficie
val ItsurOnSurface = Color.Black      // Texto principal
val ItsurOnSurfaceVariant = Color.Black     // Texto secundario
val ItsurOutline = ColorGrisFrio            // Bordes -> #8A9198

// Colores de Estatus Institucional (mantenidos para retroalimentación semántica accesible)
val StatusAprobado = Color(0xFF2E7D32)
val StatusAprobadoBg = Color(0xFFE8F5E9)
val StatusPendiente = Color(0xFFED6C02)
val StatusPendienteBg = Color(0xFFFFF3E0)
val StatusRechazado = Color(0xFFD32F2F)
val StatusRechazadoBg = Color(0xFFFFEBEE)
val StatusObservaciones = Color(0xFF0288D1)
val StatusObservacionesBg = Color(0xFFE1F5FE)

// Tokens de Material 3 Modo Claro
val primaryLight = ItsurCiruela
val onPrimaryLight = Color.White
val primaryContainerLight = ColorAzulCielo
val onPrimaryContainerLight = ItsurVinoOscuro

val secondaryLight = ColorPizarra
val onSecondaryLight = Color.Black
val secondaryContainerLight = ColorAzulPastel
val onSecondaryContainerLight = Color.Black

val tertiaryLight = ColorGrisAzulado2
val onTertiaryLight = Color.Black
val tertiaryContainerLight = ColorAzulAcero
val onTertiaryContainerLight = Color.Black

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color.White
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color.Black

val backgroundLight = ItsurOffWhite
val onBackgroundLight = ItsurOnSurface
val surfaceLight = ItsurOffWhite
val onSurfaceLight = ItsurOnSurface
val surfaceVariantLight = ItsurSurfaceVariant
val onSurfaceVariantLight = ItsurOnSurfaceVariant
val outlineLight = ItsurOutline
val outlineVariantLight = ColorAzulAcero
val scrimLight = Color(0xFF000000)

val inverseSurfaceLight = Color(0xFF2A3644)
val inverseOnSurfaceLight = Color.White
val inversePrimaryLight = Color.White
val surfaceDimLight = Color(0xFFDDE4ED)
val surfaceBrightLight = Color(0xFFF4F7FB)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFEFF3F8)
val surfaceContainerLight = Color(0xFFE7EDF4)
val surfaceContainerHighLight = Color(0xFFDFE6EE)
val surfaceContainerHighestLight = Color(0xFFD7E0E8)
