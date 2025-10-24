package com.example.mirai.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeScreen con soporte para Dark/Light Mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    userName: String,
    isDarkTheme: Boolean = true,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToCreate: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var entries by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val secondaryColor = if (isDarkTheme) MiraiPurple else MiraiGreen
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark

    // Cargar entradas
    LaunchedEffect(Unit) {
        try {
            val loadedEntries = storageManager.getAllEntries()
            entries = loadedEntries.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            error = "Error loading entries"
        } finally {
            loading = false
        }
    }

    // Fondo con degradado según tema
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Hello, $userName!",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = textColor
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToCalendar) {
                            Icon(
                                Icons.Default.CalendarToday,
                                contentDescription = "Calendar",
                                tint = primaryColor
                            )
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = secondaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToCreate,
                    containerColor = primaryColor,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Entry")
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when {
                    loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = primaryColor
                        )
                    }
                    error != null -> {
                        Text(
                            text = error ?: "",
                            color = primaryColor,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    entries.isEmpty() -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "📖",
                                style = MaterialTheme.typography.displayLarge
                            )
                            Text(
                                text = "No entries yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = textColor.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "Tap + to start writing!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = secondaryColor.copy(alpha = 0.8f)
                            )
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(entries) { entry ->
                                DiaryEntryCard(
                                    entry = entry,
                                    isDarkTheme = isDarkTheme,
                                    primaryColor = primaryColor,
                                    textColor = textColor
                                ) {
                                    onNavigateToDetail(entry.id)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Card de entrada adaptada al tema
 */
@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    isDarkTheme: Boolean,
    primaryColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkTheme) {
                Color(0xFF2D1B3D).copy(alpha = 0.6f)  // Púrpura oscuro
            } else {
                Color(0xFFE8F5EE).copy(alpha = 0.8f)  // Verde muy claro
            }
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título
            Text(
                text = entry.title.ifBlank { "(Untitled)" },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(4.dp))

            // Fecha
            val formattedDate = remember(entry.createdAt) {
                SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                    .format(Date(entry.createdAt))
            }

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = primaryColor.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(8.dp))

            // Contenido preview
            Text(
                text = entry.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}