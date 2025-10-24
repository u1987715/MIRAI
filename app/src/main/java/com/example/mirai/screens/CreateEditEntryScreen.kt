@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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
 * CreateEditEntryScreen COMPLETA
 * - Soporte de temas (Dark/Light)
 * - Imágenes y Audio
 * - Fondo degradado
 */
@Composable
fun CreateEditEntryScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    entryId: String? = null,
    isDarkTheme: Boolean = true
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado UI
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var imagePaths by remember { mutableStateOf(listOf<String>()) }
    var audioPaths by remember { mutableStateOf(listOf<String>()) }

    val isEditing = entryId != null

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val secondaryColor = if (isDarkTheme) MiraiPurple else MiraiGreen
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark

    // Cargar entrada existente si se edita
    LaunchedEffect(entryId) {
        if (isEditing) {
            try {
                val entry = storageManager.getAllEntries().find { it.id == entryId }
                entry?.let {
                    title = it.title
                    content = it.content
                    createdAt = it.createdAt
                    imagePaths = it.imagePaths
                    audioPaths = it.audioPaths
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error cargando entrada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fondo con degradado según tema
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (isEditing) "Edit Entry" else "New Entry",
                            color = textColor
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
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Campo de Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title", color = primaryColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = secondaryColor.copy(alpha = 0.5f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = secondaryColor
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
                )

                // Fecha de creación
                Text(
                    text = "Created on ${
                        SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                            .format(Date(createdAt))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = primaryColor.copy(alpha = 0.7f)
                )

                // Campo de Contenido
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Content", color = primaryColor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = secondaryColor.copy(alpha = 0.5f),
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        cursorColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        unfocusedLabelColor = secondaryColor
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
                )

                // Galería de imágenes
                if (imagePaths.isNotEmpty()) {
                    Text(
                        text = "Images",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(imagePaths) { path ->
                            AsyncImage(
                                model = path,
                                contentDescription = "Entry image",
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(4.dp)
                            )
                        }
                    }
                }

                // Lista de audios
                if (audioPaths.isNotEmpty()) {
                    Text(
                        text = "Audio Files",
                        style = MaterialTheme.typography.titleSmall,
                        color = textColor
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        audioPaths.forEachIndexed { index, _ ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDarkTheme) {
                                        Color(0xFF2D1B3D).copy(alpha = 0.4f)
                                    } else {
                                        Color(0xFFE8F5EE).copy(alpha = 0.6f)
                                    }
                                )
                            ) {
                                Text(
                                    text = "🎵 Audio ${index + 1}",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor
                                )
                            }
                        }
                    }
                }

                // Botones de adjuntar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(ctx, "Attach image - coming soon!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryColor
                        )
                    ) {
                        Text("📷 Attach Image")
                    }

                    OutlinedButton(
                        onClick = {
                            Toast.makeText(ctx, "Record audio - coming soon!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = secondaryColor
                        )
                    ) {
                        Text("🎤 Record Audio")
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Botón Guardar
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                if (title.isBlank() && content.isBlank()) {
                                    Toast.makeText(ctx, "Entry is empty", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }

                                if (isEditing) {
                                    val updated = DiaryEntry(
                                        id = entryId!!,
                                        title = title,
                                        content = content,
                                        createdAt = createdAt,
                                        imagePaths = imagePaths,
                                        audioPaths = audioPaths
                                    )
                                    storageManager.updateEntry(updated)
                                    Toast.makeText(ctx, "Entry updated", Toast.LENGTH_SHORT).show()
                                } else {
                                    val newEntry = DiaryEntry(
                                        id = UUID.randomUUID().toString(),
                                        title = title,
                                        content = content,
                                        createdAt = System.currentTimeMillis(),
                                        imagePaths = imagePaths,
                                        audioPaths = audioPaths
                                    )
                                    storageManager.createEntry(newEntry)
                                    Toast.makeText(ctx, "Entry created", Toast.LENGTH_SHORT).show()
                                }

                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                    launchSingleTop = true
                                }

                            } catch (e: Exception) {
                                Toast.makeText(ctx, "Error saving entry: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (isEditing) "Update" else "Save")
                }

                // Botón Eliminar (solo si está editando)
                if (isEditing) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                try {
                                    storageManager.deleteEntry(entryId!!)
                                    Toast.makeText(ctx, "Entry deleted", Toast.LENGTH_SHORT).show()
                                    navController.navigate("home") {
                                        popUpTo("home") { inclusive = true }
                                        launchSingleTop = true
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(ctx, "Error deleting entry", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Delete Entry")
                    }
                }
            }
        }
    }
}