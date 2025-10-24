package com.example.mirai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ========================================
// DARK MODE - Rosa/Púrpura
// ========================================

private val DarkColorScheme = darkColorScheme(
    // Colores principales
    primary = MiraiPink,                      // Rosa principal
    onPrimary = MiraiTextLight,
    primaryContainer = MiraiPurpleDark,
    onPrimaryContainer = MiraiTextLight,

    // Colores secundarios
    secondary = MiraiPurple,                  // Púrpura
    onSecondary = MiraiTextLight,
    secondaryContainer = MiraiDarkPurple,
    onSecondaryContainer = MiraiLavender,

    // Colores terciarios
    tertiary = MiraiOrange,
    onTertiary = MiraiTextLight,
    tertiaryContainer = MiraiPinkDark,
    onTertiaryContainer = Color(0xFFF5E1E8),

    // Fondo y superficies
    background = MiraiDarkPurple,
    onBackground = MiraiTextLight,
    surface = MiraiDarkGray,
    onSurface = MiraiTextLight,
    surfaceVariant = MiraiDarkPurple,
    onSurfaceVariant = MiraiLavender,

    // Otros
    outline = MiraiPurple,
    outlineVariant = MiraiPurpleDark,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

// ========================================
// LIGHT MODE - Verde/Turquesa/Azul
// ========================================

private val LightColorScheme = lightColorScheme(
    // Colores principales
    primary = MiraiTeal,                      // Turquesa principal
    onPrimary = Color.White,
    primaryContainer = MiraiLightTeal,        // Turquesa muy claro
    onPrimaryContainer = MiraiTealDark,

    // Colores secundarios
    secondary = MiraiGreen,                   // Verde
    onSecondary = Color.White,
    secondaryContainer = MiraiLightGreen,
    onSecondaryContainer = MiraiGreenDark,

    // Colores terciarios
    tertiary = MiraiCyan,                     // Azul claro
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6EEF7),
    onTertiaryContainer = MiraiCyanDark,

    // Fondo y superficies
    background = MiraiWhite,                  // Blanco
    onBackground = MiraiTextDark,
    surface = MiraiLightGray,                 // Gris muy claro
    onSurface = MiraiTextDark,
    surfaceVariant = MiraiLightGreen,
    onSurfaceVariant = MiraiGreenDark,

    // Otros
    outline = MiraiTeal,
    outlineVariant = MiraiMint,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

/**
 * Tema principal de Mirai con soporte para Dark y Light Mode
 *
 * @param darkTheme Si usar tema oscuro (true) o claro (false)
 * @param fontScale Escala de fuente (0.85f pequeña, 1.0f normal, 1.15f grande)
 * @param content Contenido de la app
 */
@Composable
fun MiraiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit
) {
    // Seleccionar esquema de color según el tema
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    // Tipografía escalada
    val scaledTypography = Typography(
        displayLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (57 * fontScale).sp,
            lineHeight = (64 * fontScale).sp,
            letterSpacing = (-0.25 * fontScale).sp
        ),
        displayMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (45 * fontScale).sp,
            lineHeight = (52 * fontScale).sp,
            letterSpacing = (0 * fontScale).sp
        ),
        displaySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (36 * fontScale).sp,
            lineHeight = (44 * fontScale).sp,
            letterSpacing = (0 * fontScale).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (32 * fontScale).sp,
            lineHeight = (40 * fontScale).sp,
            letterSpacing = (0 * fontScale).sp
        ),
        headlineMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (28 * fontScale).sp,
            lineHeight = (36 * fontScale).sp,
            letterSpacing = (0 * fontScale).sp
        ),
        headlineSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Bold,
            fontSize = (24 * fontScale).sp,
            lineHeight = (32 * fontScale).sp,
            letterSpacing = (0 * fontScale).sp
        ),
        titleLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (22 * fontScale).sp,
            lineHeight = (28 * fontScale).sp,
            letterSpacing = (0 * fontScale).sp
        ),
        titleMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.SemiBold,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = (0.15 * fontScale).sp
        ),
        titleSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = (0.1 * fontScale).sp
        ),
        bodyLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (16 * fontScale).sp,
            lineHeight = (24 * fontScale).sp,
            letterSpacing = (0.5 * fontScale).sp
        ),
        bodyMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = (0.25 * fontScale).sp
        ),
        bodySmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = (0.4 * fontScale).sp
        ),
        labelLarge = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (14 * fontScale).sp,
            lineHeight = (20 * fontScale).sp,
            letterSpacing = (0.1 * fontScale).sp
        ),
        labelMedium = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (12 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = (0.5 * fontScale).sp
        ),
        labelSmall = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            fontSize = (11 * fontScale).sp,
            lineHeight = (16 * fontScale).sp,
            letterSpacing = (0.5 * fontScale).sp
        )
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}