@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pantalla para crear o editar una entrada del diario.
 * - Si entryId es null → modo creación
 * - Si entryId tiene valor → modo edición
 */
@Composable
fun CreateEditEntryScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    entryId: String? = null // null → modo creación
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estado UI
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var createdAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var imagePaths by remember { mutableStateOf(listOf<String>()) }

    val isEditing = entryId != null

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
                }
            } catch (e: Exception) {
                Toast.makeText(ctx, "Error cargando entrada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Entry" else "New Entry") },
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next)
            )

            Text(
                text = "Created on ${
                    SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
                        .format(Date(createdAt))
                }",
                style = MaterialTheme.typography.bodySmall
            )

            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
            )

            if (imagePaths.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(imagePaths) { path ->
                        AsyncImage(
                            model = path,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(onClick = {
                    Toast.makeText(ctx, "Attach image - coming soon!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Attach Image")
                }

                Button(onClick = {
                    Toast.makeText(ctx, "Record audio - coming soon!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Record Audio")
                }
            }

            Spacer(Modifier.height(16.dp))

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
                                    imagePaths = imagePaths
                                )
                                storageManager.updateEntry(updated)
                            } else {
                                val newEntry = DiaryEntry(
                                    id = UUID.randomUUID().toString(),
                                    title = title,
                                    content = content,
                                    createdAt = System.currentTimeMillis(),
                                    imagePaths = imagePaths
                                )
                                storageManager.createEntry(newEntry)
                            }

                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }

                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Error saving entry", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }

            if (isEditing) {
                Button(
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete")
                }
            }
        }
    }
}
