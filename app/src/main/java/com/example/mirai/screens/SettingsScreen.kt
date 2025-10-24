package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.UserPreferences
import kotlinx.coroutines.launch
import androidx.compose.foundation.clickable


/**
 * SettingsScreen.kt
 * Permite cambiar el tema, el tamaño de fuente, la tipografía y el nombre del usuario.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    storageManager: LocalStorageManager
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado inicial de las preferencias
    var userPrefs by remember { mutableStateOf(UserPreferences()) }
    var loading by remember { mutableStateOf(true) }

    // Cargar preferencias
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                userPrefs = storageManager.getPreferences()
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error cargando preferencias", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

// Estados con tipo explícito
    var isDarkTheme by remember { mutableStateOf(false) }
    var fontSize by remember { mutableStateOf(1) }
    var userName by remember { mutableStateOf("") }

// Sincroniza los estados con las preferencias cargadas
    LaunchedEffect(userPrefs) {
        isDarkTheme = userPrefs.isDarkTheme
        fontSize = userPrefs.fontSize
        userName = userPrefs.userName
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Apariencia
            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            // Switch tema oscuro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Theme")
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { isDarkTheme = it }
                )
            }

            Text("Font Size", style = MaterialTheme.typography.titleSmall)
            val fontSizes = listOf("Small", "Medium", "Large")

            fontSizes.forEachIndexed { index, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { fontSize = index },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = fontSize == index,
                        onClick = { fontSize = index }
                    )
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }


            Divider()

            // Cuenta de usuario
            Text("Account", style = MaterialTheme.typography.titleMedium)
            Text("Logged in as: $userName")

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    scope.launch {
                        val updated = userPrefs.copy(
                            userName = "",
                            isDarkTheme = isDarkTheme,
                            fontSize = fontSize,
                        )
                        storageManager.savePreferences(updated)
                        Toast.makeText(ctx, "User cleared. Returning to Welcome.", Toast.LENGTH_SHORT).show()
                        navController.navigate("welcome") {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Change user")
            }

            Spacer(Modifier.height(8.dp))

            // Guardar cambios
            Button(
                onClick = {
                    scope.launch {
                        val updated = userPrefs.copy(
                            userName = userName,
                            isDarkTheme = isDarkTheme,
                            fontSize = fontSize,
                        )
                        storageManager.savePreferences(updated)
                        Toast.makeText(ctx, "Preferences saved", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}
