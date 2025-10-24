package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.mirai.R
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.UserPreferences
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch

/**
 * WelcomeScreen con soporte para Dark/Light Mode
 * - Dark Mode: Logo rosa-púrpura + degradado oscuro
 * - Light Mode: Logo verde-turquesa + degradado claro
 */
@Composable
fun WelcomeScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    homeRoute: String = "home",
    isDarkTheme: Boolean = true
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var currentPrefs by remember { mutableStateOf(UserPreferences()) }
    var nameInput by remember { mutableStateOf("") }

    // Cargar preferencias
    LaunchedEffect(Unit) {
        try {
            currentPrefs = storageManager.getPreferences()
            nameInput = currentPrefs.userName
        } catch (e: Exception) {
            Toast.makeText(ctx, "Error cargando preferencias", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val secondaryColor = if (isDarkTheme) MiraiPurple else MiraiGreen
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark

    if (loading) {
        GradientBackground(darkTheme = isDarkTheme) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
        return
    }

    // Fondo con degradado (dark o light)
    GradientBackground(darkTheme = isDarkTheme) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val padding = if (maxWidth > 600.dp) 32.dp else 24.dp
            val verticalSpace = if (maxHeight > 700.dp) 24.dp else 16.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ========================================
                // LOGO - Cambia según el tema
                // ========================================
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isDarkTheme) {
                                // Dark Mode → Logo Rosa
                                try {
                                    R.drawable.mirai_logo_color  // MIRAIROSA.png
                                } catch (e: Exception) {
                                    R.drawable.mirai_logo
                                }
                            } else {
                                // Light Mode → Logo Verde
                                try {
                                    R.drawable.mirai_logo_light  // MIRAIVERDE.png
                                } catch (e: Exception) {
                                    R.drawable.mirai_logo
                                }
                            }
                        ),
                        contentDescription = "MIRAI Logo",
                        modifier = Modifier.size(140.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(Modifier.height(verticalSpace * 2))

                // Título MIRAI
                Text(
                    text = "MIRAI",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(verticalSpace))

                val hasName = currentPrefs.userName.isNotBlank()

                if (hasName) {
                    // ========================================
                    // USUARIO EXISTENTE
                    // ========================================
                    Text(
                        text = "Welcome, ${currentPrefs.userName}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 24.sp
                        ),
                        color = textColor,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(verticalSpace * 3))

                    // Botón Start
                    Button(
                        onClick = {
                            scope.launch {
                                navController.navigate("home/${currentPrefs.userName}") {
                                    popUpTo(navController.graph.startDestinationId) {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }

                    Spacer(Modifier.height(verticalSpace))

                    // Botón Change user
                    TextButton(
                        onClick = {
                            currentPrefs = currentPrefs.copy(userName = "")
                            nameInput = ""
                        }
                    ) {
                        Text(
                            "Change user",
                            color = secondaryColor.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                } else {
                    // ========================================
                    // NUEVO USUARIO
                    // ========================================
                    Text(
                        text = "What's your name?",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(verticalSpace * 2))

                    // Campo de texto
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Enter your name",
                                color = textColor.copy(alpha = 0.5f)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = secondaryColor.copy(alpha = 0.5f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = primaryColor
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (nameInput.isNotBlank()) {
                                    scope.launch {
                                        onStart(
                                            nameInput,
                                            currentPrefs,
                                            storageManager,
                                            navController,
                                            ctx,
                                            homeRoute
                                        )
                                    }
                                }
                            }
                        )
                    )

                    Spacer(Modifier.height(verticalSpace * 2))

                    // Botón Start
                    Button(
                        onClick = {
                            scope.launch {
                                onStart(
                                    nameInput,
                                    currentPrefs,
                                    storageManager,
                                    navController,
                                    ctx,
                                    homeRoute
                                )
                            }
                        },
                        enabled = nameInput.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White,
                            disabledContainerColor = secondaryColor.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = "Start",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Función helper para iniciar sesión
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
        val updated = currentPrefs.copy(userName = name)
        storageManager.savePreferences(updated)

        navController.navigate("home/${name}") {
            popUpTo(navController.graph.startDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    } catch (e: Exception) {
        Toast.makeText(ctx, "Error guardando el nombre", Toast.LENGTH_SHORT).show()
    }
}