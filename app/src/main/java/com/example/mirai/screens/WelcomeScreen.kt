package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.UserPreferences
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview

/**
 * Pantalla de bienvenida dinámica:
 * - Si hay userName guardado -> "Welcome, userName" + botón Start
 * - Si no hay userName -> TextField + botón Start
 *
 * Navega a la pantalla Home al pulsar Start.
 *
 * Requisitos cubiertos:
 * - Persistencia local (JSON) mediante LocalStorageManager (getPreferences/savePreferences)
 * - Manejo de errores con try/catch
 * - Diseño responsive con BoxWithConstraints
 * - Material 3
 */
@Composable
fun WelcomeScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    // ruta de navegación a Home (ajústala a vuestro grafo)
    homeRoute: String = "home"
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var currentPrefs by remember { mutableStateOf(UserPreferences()) } // userName, theme, fontSize, etc.
    var nameInput by remember { mutableStateOf("") }                    // para el TextField cuando no hay nombre

    // Cargar preferencias al abrir la pantalla
    LaunchedEffect(Unit) {
        try {
            currentPrefs = storageManager.getPreferences() // lee preferences.json
            nameInput = currentPrefs.userName              // si viene vacío, el TextField aparece
        } catch (e: Exception) {
            // Si hay error, seguimos con defaults
            Toast.makeText(ctx, "Error cargando preferencias", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    if (loading) {
        // Estado de carga simple
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val padding = if (maxWidth > 600.dp) 32.dp else 16.dp
        val verticalSpace = if (maxHeight > 700.dp) 24.dp else 12.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo / ilustración (placeholder)
            Text(
                text = "MIRAI",
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(Modifier.height(verticalSpace))

            // ¿Hay nombre guardado?
            val hasName = currentPrefs.userName.isNotBlank()

            if (hasName) {
                // Mensaje de bienvenida directa
                Text(
                    text = "Welcome, ${currentPrefs.userName}",
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                // Pedir nombre por primera vez
                Text(
                    text = "Welcome to Mirai!\nWhat's your name?",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(0.9f),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // Pulsar “Done” del teclado actúa como Start
                            scope.launch { onStart(nameInput, currentPrefs, storageManager, navController, ctx, homeRoute) }
                        }
                    ),
                    placeholder = { Text("Escribe tu nombre") }
                )
            }

            Spacer(Modifier.height(verticalSpace))

            // Botón principal "Start"
            Button(
                onClick = {
                    scope.launch {
                        val nameToUse = if (hasName) currentPrefs.userName else nameInput
                        onStart(nameToUse, currentPrefs, storageManager, navController, ctx, homeRoute)
                    }
                },
                enabled = hasName || nameInput.isNotBlank()
            ) {
                Text("Start")
            }

            // Botón secundario "Change user" solo si ya hay nombre
            if (hasName) {
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        // Limpiar el nombre para que vuelva a pedirlo
                        currentPrefs = currentPrefs.copy(userName = "")
                        nameInput = ""
                    }
                ) { Text("Change user") }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    // Usa un LocalStorageManager “falso” solo para previsualizar
    val fakeStorage = LocalStorageManager(LocalContext.current)

    // No necesitamos NavController real, se puede usar uno vacío
    val fakeNavController = rememberNavController()

    WelcomeScreen(
        navController = fakeNavController,
        storageManager = fakeStorage
    )
}


/**
 * Lógica de “Start”:
 * - Valida nombre
 * - Guarda preferencias actualizando userName
 * - Navega a Home
 *
 * Guardamos el nombre usando UserPreferences + LocalStorageManager.savePreferences(),
 * ya que no existe un métod saveUserName() independiente.
 */
private suspend fun onStart(
    name: String,
    currentPrefs: UserPreferences,
    storageManager: LocalStorageManager,
    navController: NavController,
    ctx: android.content.Context,
    homeRoute: String
) {
    if (name.isBlank()) {
        Toast.makeText(ctx, "Por favor, escribe tu nombre", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        // Persistimos nombre en preferences.json
        val updated = currentPrefs.copy(userName = name)
        storageManager.savePreferences(updated)
        // Navegar a Home
        navController.navigate("home/${name}") {
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
            launchSingleTop = true
        }
    } catch (e: Exception) {
        Toast.makeText(ctx, "Error guardando el nombre", Toast.LENGTH_SHORT).show()
    }
}
