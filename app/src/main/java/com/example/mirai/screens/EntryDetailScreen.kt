@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState


/**
 * EntryDetailScreen.kt
 * Muestra una entrada completa del diario:
 *  - Título, contenido, fecha, imágenes
 *  - Botones para editar o eliminar
 */
@Composable
fun EntryDetailScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    entryId: String,
    onEdit: (String) -> Unit,   // Navegar al modo edición
    onDeleteSuccess: () -> Unit // Volver a Home tras eliminar
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var entry by remember { mutableStateOf<DiaryEntry?>(null) }
    var loading by remember { mutableStateOf(true) }

    // Cargar la entrada al iniciar la pantalla
    LaunchedEffect(entryId) {
        try {
            val allEntries = storageManager.getAllEntries()
            entry = allEntries.find { it.id == entryId }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Error cargando entrada", Toast.LENGTH_SHORT).show()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = entry?.title ?: "Entry detail",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { entry?.let { navController.navigate("createEditEntry/${it.id}") } }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                entry?.let {
                                    storageManager.deleteEntry(it.id)
                                    Toast.makeText(ctx, "Entry deleted", Toast.LENGTH_SHORT).show()
                                    onDeleteSuccess()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(ctx, "Error deleting entry", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when {
                loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                entry == null -> Text("Entry not found", modifier = Modifier.align(Alignment.Center))
                else -> EntryDetailContent(entry!!)
            }
        }
    }
}

/**
 * Contenido visual de la entrada
 */
@Composable
fun EntryDetailContent(entry: DiaryEntry) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = entry.title.ifBlank { "(Untitled)" },
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        val formattedDate = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
            .format(Date(entry.createdAt))
        Text(
            text = formattedDate,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (entry.imagePaths.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(entry.imagePaths) { imagePath ->
                    AsyncImage(
                        model = imagePath,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .padding(4.dp)
                    )
                }
            }
        }

        Text(
            text = entry.content,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

/**
 * Vista previa
 */
@Preview(showBackground = true)
@Composable
fun PreviewEntryDetailScreenFull() {
    val fakeNav = rememberNavController()
    val fakeStorage = LocalStorageManager(LocalContext.current)
    val sample = DiaryEntry(
        id = "1",
        title = "Un día tranquilo",
        content = "Hoy salí a caminar y tomé algunas fotos...",
        createdAt = System.currentTimeMillis(),
        imagePaths = listOf()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(sample.title) },
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            EntryDetailContent(sample)
        }
    }
}

