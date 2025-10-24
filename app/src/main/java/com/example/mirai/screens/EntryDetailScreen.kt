@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mirai.screens

import android.media.MediaPlayer
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.MediaManager
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * EntryDetailScreen con soporte completo para:
 * - Temas Dark/Light
 * - Visualización de imágenes
 * - Reproductor de audio funcional
 * - Zoom de imágenes
 */
@Composable
fun EntryDetailScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    entryId: String,
    isDarkTheme: Boolean = true,
    onEdit: (String) -> Unit,
    onDeleteSuccess: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaManager = remember { MediaManager() }

    var entry by remember { mutableStateOf<DiaryEntry?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    // MediaPlayer para audio
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }
    var currentPlayingIndex by remember { mutableStateOf<Int?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val secondaryColor = if (isDarkTheme) MiraiPurple else MiraiGreen
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark
    val cardColor = if (isDarkTheme) {
        Color(0xFF2D1B3D).copy(alpha = 0.6f)
    } else {
        Color(0xFFE8F5EE).copy(alpha = 0.8f)
    }

    // Cargar entrada
    LaunchedEffect(entryId) {
        scope.launch {
            try {
                val allEntries = storageManager.getAllEntries()
                entry = allEntries.find { it.id == entryId }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error loading entry", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    // Limpiar MediaPlayer al salir
    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Función para reproducir/pausar audio
    fun toggleAudioPlayback(audioPath: String, index: Int) {
        try {
            if (currentPlayingIndex == index && isPlaying) {
                // Pausar
                mediaPlayer?.pause()
                isPlaying = false
            } else if (currentPlayingIndex == index && !isPlaying) {
                // Reanudar
                mediaPlayer?.start()
                isPlaying = true
            } else {
                // Reproducir nuevo audio
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(audioPath)
                    prepare()
                    start()
                    setOnCompletionListener {
                        isPlaying = false
                        currentPlayingIndex = null
                    }
                }
                currentPlayingIndex = index
                isPlaying = true
                Toast.makeText(ctx, "Playing Audio ${index + 1}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(ctx, "Error playing audio: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    // UI de carga
    if (loading) {
        GradientBackground(darkTheme = isDarkTheme) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primaryColor)
            }
        }
        return
    }

    // Entrada no encontrada
    if (entry == null) {
        GradientBackground(darkTheme = isDarkTheme) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "📄",
                        style = MaterialTheme.typography.displayLarge
                    )
                    Text(
                        text = "Entry not found",
                        color = textColor,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Text("Go Back")
                    }
                }
            }
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
                            "Entry Details",
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = primaryColor)
                        }
                    },
                    actions = {
                        IconButton(onClick = { onEdit(entryId) }) {
                            Icon(Icons.Default.Edit, "Edit", tint = primaryColor)
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "Delete",
                                tint = MaterialTheme.colorScheme.error
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
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Card principal con el contenido
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Título
                        Text(
                            text = entry!!.title.ifBlank { "(Untitled)" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        // Fecha
                        Text(
                            text = SimpleDateFormat(
                                "MMM dd, yyyy 'at' h:mm a",
                                Locale.getDefault()
                            ).format(Date(entry!!.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor.copy(alpha = 0.8f)
                        )

                        Spacer(Modifier.height(16.dp))

                        // Contenido
                        Text(
                            text = entry!!.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                    }
                }

                // Sección de Imágenes
                if (entry!!.imagePaths.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Images (${entry!!.imagePaths.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = primaryColor
                            )
                        }

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(entry!!.imagePaths) { path ->
                                Card(
                                    modifier = Modifier
                                        .size(180.dp)
                                        .clickable {
                                            selectedImagePath = path
                                            showImageDialog = true
                                        },
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Entry image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        // Indicador de zoom
                                        Icon(
                                            imageVector = Icons.Default.ZoomIn,
                                            contentDescription = "Tap to zoom",
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(8.dp)
                                                .size(24.dp),
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Sección de Audio
                if (entry!!.audioPaths.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Audio Files (${entry!!.audioPaths.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = textColor,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = secondaryColor
                            )
                        }

                        entry!!.audioPaths.forEachIndexed { index, audioPath ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        toggleAudioPlayback(audioPath, index)
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentPlayingIndex == index && isPlaying) {
                                        primaryColor.copy(alpha = 0.3f)
                                    } else {
                                        cardColor
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
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
                                        // Icono de play/pause
                                        Icon(
                                            imageVector = if (currentPlayingIndex == index && isPlaying) {
                                                Icons.Default.Pause
                                            } else {
                                                Icons.Default.PlayArrow
                                            },
                                            contentDescription = if (currentPlayingIndex == index && isPlaying) {
                                                "Pause"
                                            } else {
                                                "Play"
                                            },
                                            tint = if (currentPlayingIndex == index && isPlaying) {
                                                primaryColor
                                            } else {
                                                secondaryColor
                                            },
                                            modifier = Modifier.size(32.dp)
                                        )

                                        Column {
                                            Text(
                                                text = "Audio ${index + 1}",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = textColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = if (currentPlayingIndex == index && isPlaying) {
                                                    "Now playing..."
                                                } else {
                                                    "Tap to play"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textColor.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    // Indicador visual
                                    if (currentPlayingIndex == index && isPlaying) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Playing",
                                            tint = primaryColor
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Espacio al final
                Spacer(Modifier.height(16.dp))
            }
        }

        // Dialog para ver imagen en grande
        if (showImageDialog && selectedImagePath != null) {
            AlertDialog(
                onDismissRequest = { showImageDialog = false },
                confirmButton = {
                    TextButton(onClick = { showImageDialog = false }) {
                        Text("Close", color = primaryColor)
                    }
                },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = File(selectedImagePath!!),
                            contentDescription = "Full size image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            )
        }

        // Dialog de confirmación de eliminación
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Entry") },
                text = { Text("Are you sure you want to delete this entry? This will also delete all associated images and audio files.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    // Detener audio si está reproduciendo
                                    mediaPlayer?.release()
                                    mediaPlayer = null

                                    // Eliminar archivos multimedia
                                    mediaManager.deleteEntryMedia(
                                        entry!!.imagePaths,
                                        entry!!.audioPaths
                                    )

                                    // Eliminar entrada
                                    storageManager.deleteEntry(entryId)
                                    Toast.makeText(ctx, "Entry deleted!", Toast.LENGTH_SHORT)
                                        .show()
                                    onDeleteSuccess()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Error deleting", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            }
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}