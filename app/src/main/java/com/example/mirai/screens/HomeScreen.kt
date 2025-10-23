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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * HomeScreen.kt
 * Pantalla principal del diario MIRAI
 *
 * - Muestra todas las entradas guardadas
 * - Permite crear nuevas (botón +)
 * - Permite navegar a Configuración y Calendario
 * - Si no hay entradas, muestra un mensaje vacío
 * - Al hacer clic en una entrada, navega al detalle
 */

@Composable
fun HomeScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    userName: String,                       // Nombre guardado en preferencias
    onNavigateToDetail: (String) -> Unit,   // Ir al detalle de una entrada
    onNavigateToCreate: () -> Unit,         // Crear nueva entrada
    onNavigateToCalendar: () -> Unit,       // Abrir calendario
    onNavigateToSettings: () -> Unit        // Abrir ajustes
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado: lista de entradas y control de carga
    var entries by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // Cargar las entradas al entrar en la pantalla
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

    // Diseño general con barra superior y botón flotante
    Scaffold(
        topBar = {
            HomeTopBar(
                userName = userName,
                onSettingsClick = onNavigateToSettings,
                onCalendarClick = onNavigateToCalendar
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToCreate) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                error != null -> {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                entries.isEmpty() -> {
                    Text(
                        text = "No entries yet. Tap + to start writing!",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(entries) { entry ->
                            DiaryEntryCard(entry = entry) {
                                onNavigateToDetail(entry.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Barra superior con saludo y botones de navegación
 */
@Composable
fun HomeTopBar(
    userName: String,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit
) {
    @OptIn(ExperimentalMaterial3Api::class)
    TopAppBar(
        title = {
            Text(
                text = "Hello, $userName!",
                style = MaterialTheme.typography.titleLarge
            )
        },
        actions = {
            IconButton(onClick = onCalendarClick) {
                Icon(Icons.Default.CalendarToday, contentDescription = "Calendar")
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

/**
 * Tarjeta de una entrada individual
 */
@Composable
fun DiaryEntryCard(entry: DiaryEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.title.ifBlank { "(Untitled)" },
                style = MaterialTheme.typography.titleMedium
            )

            val formattedDate = remember(entry.createdAt) {
                SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                    .format(Date(entry.createdAt))
            }

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = entry.content,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Preview con datos simulados
 */
@Preview(showBackground = true)
@Composable
fun PreviewHomeScreen() {
    val fakeNavController = rememberNavController()
    val fakeStorage = LocalStorageManager(LocalContext.current)
    val sampleEntries = listOf(
        DiaryEntry(
            id = "1",
            title = "Día productivo",
            content = "Hoy avancé mucho con el proyecto MIRAI...",
            createdAt = System.currentTimeMillis()
        ),
        DiaryEntry(
            id = "2",
            title = "Reflexión nocturna",
            content = "He aprendido bastante sobre Compose y Kotlin...",
            createdAt = System.currentTimeMillis() - 86400000
        )
    )

    Scaffold(
        topBar = {
            HomeTopBar(
                userName = "Fede",
                onSettingsClick = {},
                onCalendarClick = {}
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleEntries) { entry ->
                DiaryEntryCard(entry = entry, onClick = {})
            }
        }
    }
}
