@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * EntryDetailScreen con soporte para temas
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

    var entry by remember { mutableStateOf<DiaryEntry?>(null) }
    var loading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark
    val cardColor = if (isDarkTheme) {
        Color(0xFF2D1B3D).copy(alpha = 0.6f)
    } else {
        Color(0xFFE8F5EE).copy(alpha = 0.8f)
    }

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

    if (loading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = primaryColor)
        }
        return
    }

    if (entry == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Entry not found", color = textColor)
        }
        return
    }

    // Fondo con degradado según tema
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Entry Details", color = textColor) },
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
                            Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = cardColor)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = entry!!.title.ifBlank { "(Untitled)" },
                            style = MaterialTheme.typography.headlineMedium,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                                .format(Date(entry!!.createdAt)),
                            style = MaterialTheme.typography.bodySmall,
                            color = primaryColor.copy(alpha = 0.7f)
                        )

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = entry!!.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor
                        )
                    }
                }

                if (entry!!.imagePaths.isNotEmpty()) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(entry!!.imagePaths) { path ->
                            AsyncImage(
                                model = path,
                                contentDescription = "Entry image",
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Audios
                if (entry!!.audioPaths.isNotEmpty()) {
                    Text(
                        text = "Audio Files",
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        entry!!.audioPaths.forEachIndexed { index, _ ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = cardColor
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🎵",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    Text(
                                        text = "Audio ${index + 1}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Entry") },
                text = { Text("Are you sure you want to delete this entry?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    storageManager.deleteEntry(entryId)
                                    Toast.makeText(ctx, "Entry deleted", Toast.LENGTH_SHORT).show()
                                    onDeleteSuccess()
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Error deleting", Toast.LENGTH_SHORT).show()
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
    }}