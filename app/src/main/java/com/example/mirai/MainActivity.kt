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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // SDE Fix: Use applicationContext to avoid Activity context leaks in long-lived objects
            val storageManager = remember { LocalStorageManager(applicationContext) }

            // State
            var isDarkTheme by remember { mutableStateOf(true) }
            var fontScale by remember { mutableStateOf(1.0f) }

            // Scope for preference saving
            val scope = rememberCoroutineScope()

            // Initial Load
            LaunchedEffect(Unit) {
                try {
                    val prefs = storageManager.getPreferences()
                    isDarkTheme = prefs.isDarkTheme
                    fontScale = getFontSizeScale(prefs.fontSize)
                } catch (e: Exception) {
                    isDarkTheme = true
                    fontScale = 1.0f
                }
            }

            val navController = rememberNavController()

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
                            isDarkTheme = newTheme
                            scope.launch {
                                // Safe async save
                                try {
                                    val prefs = storageManager.getPreferences()
                                    storageManager.savePreferences(
                                        prefs.copy(isDarkTheme = newTheme)
                                    )
                                } catch (e: Exception) {
                                    // Log error if needed
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}