package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.UserPreferences
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch

/**
 * SettingsScreen con Theme Toggle funcional
 * - Cambiar entre Dark/Light Mode
 * - Cambiar tamaño de fuente
 * - Cambiar nombre de usuario
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    isDarkTheme: Boolean = true,
    onThemeChange: (Boolean) -> Unit = {}
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado inicial de las preferencias
    var userPrefs by remember { mutableStateOf(UserPreferences()) }
    var loading by remember { mutableStateOf(true) }

    // Estados locales
    var localIsDarkTheme by remember { mutableStateOf(isDarkTheme) }
    var fontSize by remember { mutableStateOf(1) }
    var userName by remember { mutableStateOf("") }
    var showNameDialog by remember { mutableStateOf(false) }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val secondaryColor = if (isDarkTheme) MiraiPurple else MiraiGreen
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark
    val cardColor = if (isDarkTheme) {
        Color(0xFF2D1B3D).copy(alpha = 0.6f)
    } else {
        Color(0xFFE8F5EE).copy(alpha = 0.8f)
    }

    // Cargar preferencias
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                userPrefs = storageManager.getPreferences()
                localIsDarkTheme = userPrefs.isDarkTheme
                fontSize = userPrefs.fontSize
                userName = userPrefs.userName
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error cargando preferencias", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryColor)
        }
        return
    }

    // Fondo con degradado según tema
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Settings",
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = primaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ========================================
                // SECCIÓN: APARIENCIA
                // ========================================
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )

                // Theme Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (localIsDarkTheme) {
                                    Icons.Default.DarkMode
                                } else {
                                    Icons.Default.LightMode
                                },
                                contentDescription = "Theme",
                                tint = primaryColor
                            )
                            Column {
                                Text(
                                    text = "Theme",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (localIsDarkTheme) "Dark Mode" else "Light Mode",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Switch(
                            checked = localIsDarkTheme,
                            onCheckedChange = { newValue ->
                                localIsDarkTheme = newValue

                                // Actualizar inmediatamente
                                onThemeChange(newValue)

                                // Guardar preferencia
                                scope.launch {
                                    try {
                                        val updated = userPrefs.copy(isDarkTheme = newValue)
                                        storageManager.savePreferences(updated)
                                        userPrefs = updated
                                        Toast.makeText(
                                            ctx,
                                            "Theme changed to ${if (newValue) "Dark" else "Light"} Mode",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            ctx,
                                            "Error saving theme",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = primaryColor,
                                checkedTrackColor = primaryColor.copy(alpha = 0.5f),
                                uncheckedThumbColor = secondaryColor,
                                uncheckedTrackColor = secondaryColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                }

                // Font Size
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Font Size",
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Small
                            FilterChip(
                                selected = fontSize == 0,
                                onClick = {
                                    fontSize = 0
                                    scope.launch {
                                        try {
                                            val updated = userPrefs.copy(fontSize = 0)
                                            storageManager.savePreferences(updated)
                                            userPrefs = updated
                                            Toast.makeText(ctx, "Font size: Small", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Error saving", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                label = { Text("Small") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            // Normal
                            FilterChip(
                                selected = fontSize == 1,
                                onClick = {
                                    fontSize = 1
                                    scope.launch {
                                        try {
                                            val updated = userPrefs.copy(fontSize = 1)
                                            storageManager.savePreferences(updated)
                                            userPrefs = updated
                                            Toast.makeText(ctx, "Font size: Normal", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Error saving", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                label = { Text("Normal") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.White
                                )
                            )

                            // Large
                            FilterChip(
                                selected = fontSize == 2,
                                onClick = {
                                    fontSize = 2
                                    scope.launch {
                                        try {
                                            val updated = userPrefs.copy(fontSize = 2)
                                            storageManager.savePreferences(updated)
                                            userPrefs = updated
                                            Toast.makeText(ctx, "Font size: Large", Toast.LENGTH_SHORT).show()
                                        } catch (e: Exception) {
                                            Toast.makeText(ctx, "Error saving", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                label = { Text("Large") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = primaryColor,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // ========================================
                // SECCIÓN: ACCOUNT
                // ========================================
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    color = primaryColor,
                    fontWeight = FontWeight.Bold
                )

                // User Name
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Username",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = userName.ifBlank { "Not set" },
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }

                        TextButton(onClick = { showNameDialog = true }) {
                            Text("Change", color = primaryColor)
                        }
                    }
                }
            }
        }

        // Dialog para cambiar nombre
        if (showNameDialog) {
            var newName by remember { mutableStateOf(userName) }

            AlertDialog(
                onDismissRequest = { showNameDialog = false },
                title = { Text("Change Username") },
                text = {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("New username") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newName.isNotBlank()) {
                                scope.launch {
                                    try {
                                        val updated = userPrefs.copy(userName = newName)
                                        storageManager.savePreferences(updated)
                                        userPrefs = updated
                                        userName = newName
                                        showNameDialog = false
                                        Toast.makeText(ctx, "Username updated", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "Error saving", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showNameDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }}