package com.example.mirai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.mirai.ui.theme.*

/**
 * Fondo con degradado que se adapta al tema
 * - Dark Mode: Negro → Rosa-Púrpura
 * - Light Mode: Blanco → Verde-Turquesa-Azul
 */
@Composable
fun GradientBackground(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (darkTheme) {
                    // ========================================
                    // DARK MODE - Negro → Rosa-Púrpura
                    // ========================================
                    Brush.verticalGradient(
                        colors = listOf(
                            // Arriba - degradado oscuro
                            Color(0xFF0A0412),           // Púrpura muy muy oscuro - 0%
                            Color(0xFF0D0616),           // Púrpura casi negro - 10%
                            Color(0xFF12081C),           // Púrpura oscuro - 20%
                            Color(0xFF1A0B24),           // Púrpura oscuro - 30%

                            // Medio - transición suave
                            Color(0xFF220F2D),           // Púrpura medio oscuro - 40%
                            Color(0xFF2D1538),           // Púrpura - 50%
                            Color(0xFF3A1C45),           // Púrpura medio - 60%

                            // Abajo - rosas oscuros
                            Color(0xFF4A2550),           // Púrpura-rosa oscuro - 70%
                            Color(0xFF5C2F5C),           // Rosa-púrpura oscuro - 80%
                            Color(0xFF6E3A66),           // Rosa oscuro - 90%
                            Color(0xFF7A4569)            // Rosa medio oscuro - 100%
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                } else {
                    // ========================================
                    // LIGHT MODE - Blanco → Verde-Turquesa-Azul
                    // ========================================
                    Brush.verticalGradient(
                        colors = listOf(
                            // Arriba - blanco/muy claro
                            Color(0xFFFFFFFF),           // Blanco puro - 0%
                            Color(0xFFFAFDFB),           // Blanco con tinte verde - 10%
                            Color(0xFFF5FBF8),           // Blanco verdoso muy claro - 20%
                            Color(0xFFF0F9F5),           // Blanco verdoso claro - 30%

                            // Medio - transición a colores
                            Color(0xFFE8F5EE),           // Verde muy claro - 40%
                            Color(0xFFDDF2E8),           // Verde claro - 50%
                            Color(0xFFD0EEE1),           // Verde-turquesa claro - 60%

                            // Abajo - colores del logo
                            Color(0xFFC2E9D9),           // Verde medio - 70%
                            Color(0xFFB3E4D1),           // Verde-turquesa - 80%
                            Color(0xFFA5DFC9),           // Turquesa claro - 90%
                            Color(0xFF98DAC3)            // Turquesa-verde - 100%
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                }
            )
    ) {
        content()
    }
}

/**
 * Variante suave para pantallas con contenido (HomeScreen, etc)
 */
@Composable
fun SoftGradientBackground(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (darkTheme) {
                    // Dark Mode - Más suave
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0412),
                            Color(0xFF0D0616),
                            Color(0xFF12081C),
                            Color(0xFF1A0B24),
                            Color(0xFF220F2D),
                            Color(0xFF2D1538).copy(alpha = 0.8f),
                            Color(0xFF3A1C45).copy(alpha = 0.6f),
                            Color(0xFF4A2550).copy(alpha = 0.4f),
                            Color(0xFF5C2F5C).copy(alpha = 0.3f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                } else {
                    // Light Mode - Más suave
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFFAFDFB),
                            Color(0xFFF5FBF8),
                            Color(0xFFF0F9F5),
                            Color(0xFFE8F5EE).copy(alpha = 0.8f),
                            Color(0xFFDDF2E8).copy(alpha = 0.6f),
                            Color(0xFFD0EEE1).copy(alpha = 0.4f),
                            Color(0xFFC2E9D9).copy(alpha = 0.3f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                }
            )
    ) {
        content()
    }
}

/**
 * Degradado horizontal para cards especiales
 */
@Composable
fun HorizontalGradientCard(
    darkTheme: Boolean = isSystemInDarkTheme(),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .background(
                brush = if (darkTheme) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6E3A66),  // Rosa oscuro
                            Color(0xFF5C2F5C),  // Púrpura-rosa
                            Color(0xFF4A2550)   // Púrpura oscuro
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFA5DFC9),  // Turquesa-verde
                            Color(0xFF8FC9C9),  // Turquesa
                            Color(0xFF89C5D9)   // Azul claro
                        )
                    )
                }
            )
    ) {
        content()
    }
}