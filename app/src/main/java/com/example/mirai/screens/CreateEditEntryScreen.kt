package com.example.mirai.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.data.MediaManager
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.utils.MediaHelper
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * CreateEditEntryScreen con soporte multimedia completo:
 * - Galería (múltiples imágenes)
 * - Cámara (tomar fotos)
 * - Grabación de audio
 * - Eliminar multimedia
 * - Permisos automáticos
 * - Temas Dark/Light
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditEntryScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    entryId: String? = null,
    isDarkTheme: Boolean = true
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val mediaManager = remember { MediaManager() }

    // Estado
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imagePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var audioPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // Variables para MediaRecorder
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }

    // URI temporal para cámara
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark

    // Cargar entrada existente
    LaunchedEffect(entryId) {
        entryId?.let { id ->
            try {
                val entries = storageManager.getAllEntries()
                val entry = entries.find { it.id == id }
                entry?.let {
                    title = it.title
                    content = it.content
                    imagePaths = it.imagePaths
                    audioPaths = it.audioPaths
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error loading entry", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher para galería (múltiples imágenes)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                try {
                    val newPaths = uris.mapNotNull { uri ->
                        try {
                            mediaManager.saveImage(context, uri)
                        } catch (e: Exception) {
                            null
                        }
                    }
                    imagePaths = imagePaths + newPaths
                    Toast.makeText(context, "${newPaths.size} images added", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error adding images", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            scope.launch {
                try {
                    val savedPath = mediaManager.saveImage(context, tempCameraUri!!)
                    imagePaths = imagePaths + savedPath
                    Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving photo", Toast.LENGTH_SHORT).show()
                }
                tempCameraUri = null
            }
        } else {
            tempCameraUri = null
        }
    }

    // Launcher para permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (permissionType) {
                "camera" -> {
                    try {
                        val timestamp = System.currentTimeMillis()
                        val imageFile = File(context.filesDir, "images/IMG_$timestamp.jpg")
                        imageFile.parentFile?.mkdirs()

                        tempCameraUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            imageFile
                        )
                        cameraLauncher.launch(tempCameraUri!!)
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error opening camera", Toast.LENGTH_SHORT).show()
                    }
                }
                "audio" -> {
                    val result = MediaHelper.startRecording(context)
                    if (result != null) {
                        mediaRecorder = result.first
                        currentAudioFile = result.second
                        isRecording = true
                        Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error starting recording", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } else {
            showPermissionDialog = true
        }
    }

    // Funciones
    fun handleGalleryClick() {
        galleryLauncher.launch("image/*")
    }

    fun handleCameraClick() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                try {
                    val timestamp = System.currentTimeMillis()
                    val imageFile = File(context.filesDir, "images/IMG_$timestamp.jpg")
                    imageFile.parentFile?.mkdirs()

                    tempCameraUri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        imageFile
                    )
                    cameraLauncher.launch(tempCameraUri!!)
                } catch (e: Exception) {
                    Toast.makeText(context, "Error opening camera", Toast.LENGTH_SHORT).show()
                }
            }
            else -> {
                permissionType = "camera"
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    fun handleRecordClick() {
        if (isRecording) {
            // Detener grabación
            if (MediaHelper.stopRecording(mediaRecorder)) {
                currentAudioFile?.let { file ->
                    audioPaths = audioPaths + file.absolutePath
                    Toast.makeText(context, "Audio saved!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Error stopping recording", Toast.LENGTH_SHORT).show()
            }
            mediaRecorder = null
            currentAudioFile = null
            isRecording = false
        } else {
            // Iniciar grabación
            when {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED -> {
                    val result = MediaHelper.startRecording(context)
                    if (result != null) {
                        mediaRecorder = result.first
                        currentAudioFile = result.second
                        isRecording = true
                        Toast.makeText(context, "Recording started", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Error starting recording", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {
                    permissionType = "audio"
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    fun handleSave() {
        if (title.isBlank() && content.isBlank()) {
            Toast.makeText(context, "Please add a title or content", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            loading = true
            try {
                val entry = DiaryEntry(
                    id = entryId ?: UUID.randomUUID().toString(),
                    title = title,
                    content = content,
                    createdAt = if (entryId != null) {
                        // Mantener fecha original si es edición
                        val entries = storageManager.getAllEntries()
                        entries.find { it.id == entryId }?.createdAt ?: System.currentTimeMillis()
                    } else {
                        System.currentTimeMillis()
                    },
                    imagePaths = imagePaths,
                    audioPaths = audioPaths
                )

                if (entryId != null) {
                    storageManager.updateEntry(entry)
                    Toast.makeText(context, "Entry updated!", Toast.LENGTH_SHORT).show()
                } else {
                    storageManager.createEntry(entry)
                    Toast.makeText(context, "Entry created!", Toast.LENGTH_SHORT).show()
                }

                navController.popBackStack()
            } catch (e: Exception) {
                Toast.makeText(context, "Error saving entry", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    fun handleDelete() {
        entryId?.let { id ->
            scope.launch {
                loading = true
                try {
                    // Eliminar archivos multimedia
                    mediaManager.deleteEntryMedia(imagePaths, audioPaths)

                    // Eliminar entrada
                    storageManager.deleteEntry(id)
                    Toast.makeText(context, "Entry deleted!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                } catch (e: Exception) {
                    Toast.makeText(context, "Error deleting entry", Toast.LENGTH_SHORT).show()
                } finally {
                    loading = false
                }
            }
        }
    }

    // Limpiar recursos al salir
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                MediaHelper.stopRecording(mediaRecorder)
                mediaRecorder = null
            }
        }
    }

    // UI
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (entryId != null) "Edit Entry" else "New Entry",
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
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
        ) { padding ->
            if (loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo de título
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = primaryColor
                        )
                    )

                    // Fecha
                    Text(
                        text = "Created on: ${
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(Date())
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = textColor.copy(alpha = 0.7f)
                    )

                    // Campo de contenido
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content", color = primaryColor) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            unfocusedBorderColor = textColor.copy(alpha = 0.5f),
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            cursorColor = primaryColor
                        )
                    )

                    // Sección de imágenes
                    if (imagePaths.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Images (${imagePaths.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                imagePaths.take(3).forEach { path ->
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = File(path),
                                            contentDescription = "Image",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Botón eliminar
                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    mediaManager.deleteMediaFile(path)
                                                    imagePaths = imagePaths - path
                                                }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .size(32.dp)
                                                .background(
                                                    Color.Black.copy(alpha = 0.6f),
                                                    RoundedCornerShape(16.dp)
                                                )
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Delete",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (imagePaths.size > 3) {
                                Text(
                                    text = "+ ${imagePaths.size - 3} more",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = primaryColor
                                )
                            }
                        }
                    }

                    // Sección de audios
                    if (audioPaths.isNotEmpty()) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Audio Files (${audioPaths.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )

                            audioPaths.forEachIndexed { index, path ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isDarkTheme) {
                                            Color(0xFF2D1B3D).copy(alpha = 0.6f)
                                        } else {
                                            Color(0xFFE8F5EE).copy(alpha = 0.8f)
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MusicNote,
                                                contentDescription = "Audio",
                                                tint = primaryColor
                                            )
                                            Text(
                                                text = "Audio ${index + 1}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textColor
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    mediaManager.deleteMediaFile(path)
                                                    audioPaths = audioPaths - path
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Botones de multimedia
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Botón galería
                        OutlinedButton(
                            onClick = { handleGalleryClick() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery")
                        }

                        // Botón cámara
                        OutlinedButton(
                            onClick = { handleCameraClick() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = primaryColor
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera")
                        }
                    }

                    // Botón grabar audio
                    Button(
                        onClick = { handleRecordClick() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording)
                                MaterialTheme.colorScheme.error
                            else
                                primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = if (isRecording)
                                Icons.Default.Stop
                            else
                                Icons.Default.Mic,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (isRecording) "⏹️ Stop Recording" else "🎤 Record Audio",
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón guardar
                    Button(
                        onClick = { handleSave() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = title.isNotBlank() || content.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Entry", fontSize = 16.sp)
                    }

                    // Botón eliminar (solo si es edición)
                    if (entryId != null) {
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete Entry", fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        // Dialog de confirmación de eliminación
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Entry") },
                text = { Text("Are you sure you want to delete this entry? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            handleDelete()
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

        // Dialog de permiso denegado
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = {
                    Text(
                        "This feature requires ${
                            if (permissionType == "camera") "camera" else "microphone"
                        } permission. Please grant it in Settings."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}