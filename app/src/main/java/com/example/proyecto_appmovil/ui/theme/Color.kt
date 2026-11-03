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
val ItsurVinoOscuro = Color(0xFF112233)     // Títulos principales y texto de máximo contraste
val ItsurRosaPastel = ColorAzulCielo        // Banner de bienvenida, contenedores destacados -> #ACD0F2
val ItsurRosaSuave = ColorAzulPastel        // Tarjetas de opción y contenedores secundarios -> #A7C7E4
val ItsurOffWhite = Color(0xFFF4F7FB)       // Fondo general de las pantallas (blanco frío limpio)
val ItsurSurfaceVariant = Color(0xFFE2EAF2) // Variantes de superficie
val ItsurOnSurface = Color(0xFF121E2B)      // Texto principal
val ItsurOnSurfaceVariant = ColorGrisMedio  // Texto secundario -> #85898C
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
val onSecondaryLight = Color.White
val secondaryContainerLight = ColorAzulPastel
val onSecondaryContainerLight = Color(0xFF14202E)

val tertiaryLight = ColorGrisAzulado2
val onTertiaryLight = Color.White
val tertiaryContainerLight = ColorAzulAcero
val onTertiaryContainerLight = Color(0xFF101B27)

val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color.White
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)

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
val inverseOnSurfaceLight = Color(0xFFEEF3F8)
val inversePrimaryLight = ColorAzulCielo
val surfaceDimLight = Color(0xFFDDE4ED)
val surfaceBrightLight = Color(0xFFF4F7FB)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFEFF3F8)
val surfaceContainerLight = Color(0xFFE7EDF4)
val surfaceContainerHighLight = Color(0xFFDFE6EE)
val surfaceContainerHighestLight = Color(0xFFD7E0E8)

// Tokens de Material 3 Modo Oscuro adaptados a la paleta Azul/Gris
val primaryDark = ColorAzulCielo
val onPrimaryDark = Color(0xFF0A1E36)
val primaryContainerDark = Color(0xFF1A385C)
val onPrimaryContainerDark = ColorAzulCielo

val secondaryDark = ColorAzulAcero
val onSecondaryDark = Color(0xFF122232)
val secondaryContainerDark = Color(0xFF243B52)
val onSecondaryContainerDark = ColorAzulPastel

val tertiaryDark = ColorGrisAzulado1
val onTertiaryDark = Color(0xFF15202B)
val tertiaryContainerDark = Color(0xFF2E3E4E)
val onTertiaryContainerDark = ColorAzulAcero

val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val backgroundDark = Color(0xFF0E1722)
val onBackgroundDark = Color(0xFFDEE5EE)
val surfaceDark = Color(0xFF0E1722)
val onSurfaceDark = Color(0xFFDEE5EE)
val surfaceVariantDark = Color(0xFF334354)
val onSurfaceVariantDark = ColorGrisAzulado1
val outlineDark = ColorPizarra
val outlineVariantDark = ColorGrisAzulado2
val scrimDark = Color(0xFF000000)

val inverseSurfaceDark = Color(0xFFDEE5EE)
val inverseOnSurfaceDark = Color(0xFF0E1722)
val inversePrimaryDark = ItsurCiruela
val surfaceDimDark = Color(0xFF0E1722)
val surfaceBrightDark = Color(0xFF2A394B)
val surfaceContainerLowestDark = Color(0xFF091019)
val surfaceContainerLowDark = Color(0xFF131F2D)
val surfaceContainerDark = Color(0xFF172433)
val surfaceContainerHighDark = Color(0xFF202E3E)
val surfaceContainerHighestDark = Color(0xFF2A3849)
