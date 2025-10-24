package com.example.mirai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.getFontSizeScale
import com.example.mirai.navigation.NavGraph
import com.example.mirai.ui.theme.MiraiTheme
import kotlinx.coroutines.launch

/**
 * MainActivity con soporte para Dark/Light Mode
 *
 * El tema se guarda en preferencias y se aplica en toda la app
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permitir contenido edge-to-edge
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Storage manager
            val storageManager = remember { LocalStorageManager(this) }

            // Estado del tema (dark/light)
            var isDarkTheme by remember { mutableStateOf(true) }  // Dark por defecto
            var fontScale by remember { mutableStateOf(1.0f) }
            val scope = rememberCoroutineScope()

            // Cargar preferencias (tema y fuente)
            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        val prefs = storageManager.getPreferences()
                        isDarkTheme = prefs.isDarkTheme
                        fontScale = getFontSizeScale(prefs.fontSize)
                    } catch (e: Exception) {
                        // Usar defaults
                        isDarkTheme = true
                        fontScale = 1.0f
                    }
                }
            }

            val navController = rememberNavController()

            // Aplicar tema MIRAI
            MiraiTheme(
                darkTheme = isDarkTheme,
                fontScale = fontScale
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    NavGraph(
                        navController = navController,
                        storageManager = storageManager,
                        startDestination = "welcome",
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { newTheme ->
                            // Actualizar tema
                            isDarkTheme = newTheme

                            // Guardar preferencia
                            scope.launch {
                                try {
                                    val prefs = storageManager.getPreferences()
                                    storageManager.savePreferences(
                                        prefs.copy(isDarkTheme = newTheme)
                                    )
                                } catch (e: Exception) {
                                    // Error guardando
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * ========================================
 * TEMA DINÁMICO! ✨
 * ========================================
 *
 * Ahora MIRAI tiene 2 temas completos:
 *
 * DARK MODE:
 * - Logo: Rosa-Púrpura (MIRAIROSA.png)
 * - Degradado: Negro → Rosa-Púrpura
 * - Botones: Rosa/Púrpura
 * - Cards: Púrpura oscuro
 *
 * LIGHT MODE:
 * - Logo: Verde-Turquesa (MIRAIVERDE.png)
 * - Degradado: Blanco → Verde-Turquesa-Azul
 * - Botones: Verde/Turquesa
 * - Cards: Verde claro
 *
 * El usuario puede cambiar en Settings! 🎨
 */