package com.example.mirai.screens

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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

    // State
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var imagePaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var audioPaths by remember { mutableStateOf<List<String>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) } // Now used
    var isRecording by remember { mutableStateOf(false) }
    var permissionType by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // Media Recorder State
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var currentAudioFile by remember { mutableStateOf<File?>(null) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark

    // Load existing data
    LaunchedEffect(entryId) {
        entryId?.let { id ->
            val entries = storageManager.getAllEntries()
            entries.find { it.id == id }?.let {
                title = it.title
                content = it.content
                imagePaths = it.imagePaths
                audioPaths = it.audioPaths
            }
        }
    }

    // --- LAUNCHERS & HELPERS DEFINED BEFORE USE ---

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            scope.launch {
                try {
                    val savedPath = mediaManager.saveImage(context, tempCameraUri!!)
                    imagePaths = imagePaths + savedPath
                    Toast.makeText(context, "Photo captured!", Toast.LENGTH_SHORT).show()
                } catch (_: Exception) {
                    Toast.makeText(context, "Error saving photo", Toast.LENGTH_SHORT).show()
                }
                tempCameraUri = null
            }
        }
    }

    // Helper: Launch Camera
    fun launchCamera() {
        try {
            val timestamp = System.currentTimeMillis()
            val tempFile = File(context.cacheDir, "temp_cam_$timestamp.jpg")
            tempFile.parentFile?.mkdirs()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
            tempCameraUri = uri
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Error starting camera: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper: Start Audio
    fun startAudioRecording() {
        val result = MediaHelper.startRecording(context)
        if (result != null) {
            mediaRecorder = result.first
            currentAudioFile = result.second
            isRecording = true
        } else {
            Toast.makeText(context, "Error starting recorder", Toast.LENGTH_SHORT).show()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (permissionType == "camera") launchCamera()
            else if (permissionType == "audio") startAudioRecording()
        } else {
            showPermissionDialog = true
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                loading = true
                val newPaths = uris.mapNotNull { uri ->
                    try {
                        mediaManager.saveImage(context, uri)
                    } catch (_: Exception) {
                        null
                    }
                }
                imagePaths = imagePaths + newPaths
                loading = false
            }
        }
    }

    // --- ACTIONS ---

    fun handleGalleryClick() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
        )
    }

    fun handleCameraClick() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera()
        } else {
            permissionType = "camera"
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    fun handleRecordClick() {
        if (isRecording) {
            if (MediaHelper.stopRecording(mediaRecorder)) {
                currentAudioFile?.let { file ->
                    scope.launch {
                        try {
                            val savedPath = mediaManager.saveAudio(context, Uri.fromFile(file))
                            audioPaths = audioPaths + savedPath
                        } catch (_: Exception) {
                            Toast.makeText(context, "Error saving audio", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            mediaRecorder = null
            isRecording = false
        } else {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startAudioRecording()
            } else {
                permissionType = "audio"
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    fun handleSave() {
        if (title.isBlank() && content.isBlank()) {
            Toast.makeText(context, "Please add title or content", Toast.LENGTH_SHORT).show()
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
                        storageManager.getAllEntries().find { it.id == entryId }?.createdAt ?: System.currentTimeMillis()
                    } else System.currentTimeMillis(),
                    imagePaths = imagePaths,
                    audioPaths = audioPaths
                )

                if (entryId != null) storageManager.updateEntry(entry)
                else storageManager.createEntry(entry)

                navController.popBackStack()
            } catch (_: Exception) {
                Toast.makeText(context, "Error saving entry", Toast.LENGTH_SHORT).show()
            } finally {
                loading = false
            }
        }
    }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            if (isRecording) {
                MediaHelper.stopRecording(mediaRecorder)
                mediaRecorder = null
            }
        }
    }

    // UI Structure
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (entryId != null) "Edit Entry" else "New Entry", color = textColor) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            // Fixed: Use AutoMirrored
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = primaryColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )

                    // Date
                    Text(
                        text = "Today: ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())}",
                        color = textColor.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )

                    // Content
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Dear Diary...", color = primaryColor) },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        )
                    )

                    // --- IMAGES ---
                    if (imagePaths.isNotEmpty()) {
                        Text("Images", color = textColor, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            imagePaths.take(3).forEach { path ->
                                Box(Modifier.size(80.dp).clip(RoundedCornerShape(8.dp))) {
                                    AsyncImage(
                                        model = File(path),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Delete Button
                                    IconButton(
                                        onClick = {
                                            scope.launch {
                                                mediaManager.deleteMediaFile(path)
                                                imagePaths = imagePaths - path
                                            }
                                        },
                                        modifier = Modifier.align(Alignment.TopEnd).size(24.dp).background(Color.Black.copy(0.5f))
                                    ) {
                                        Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // --- MEDIA BUTTONS ---
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { handleGalleryClick() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                        ) {
                            Icon(Icons.Default.Image, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Gallery")
                        }
                        OutlinedButton(
                            onClick = { handleCameraClick() },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                        ) {
                            Icon(Icons.Default.CameraAlt, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Camera")
                        }
                    }

                    // --- AUDIO ---
                    if (audioPaths.isNotEmpty()) {
                        Text("Audio Notes", color = textColor, fontWeight = FontWeight.Bold)
                        audioPaths.forEach { path ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MusicNote, null, tint = primaryColor)
                                Text("Audio Note", color = textColor, modifier = Modifier.padding(start = 8.dp).weight(1f))
                                IconButton(onClick = {
                                    scope.launch {
                                        mediaManager.deleteMediaFile(path)
                                        audioPaths = audioPaths - path
                                    }
                                }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Red)
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { handleRecordClick() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if(isRecording) Color.Red else primaryColor
                        )
                    ) {
                        Icon(if(isRecording) Icons.Default.Stop else Icons.Default.Mic, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if(isRecording) "Stop Recording" else "Record Audio")
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = { handleSave() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                    ) {
                        Text("Save Entry")
                    }

                    if (entryId != null) {
                        TextButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete Entry", color = Color.Red)
                        }
                    }
                }
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete?") },
                text = { Text("Irreversible.") },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            mediaManager.deleteEntryMedia(imagePaths, audioPaths)
                            // entryId check is implicitly handled by showing this dialog only if entryId != null
                            if (entryId != null) {
                                storageManager.deleteEntry(entryId)
                                navController.popBackStack()
                            }
                        }
                    }) { Text("Delete", color = Color.Red) }
                },
                dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") } }
            )
        }

        // Fixed: Added missing Permission Dialog UI
        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = { showPermissionDialog = false },
                title = { Text("Permission Required") },
                text = { Text("Please enable permissions in Settings to use this feature.") },
                confirmButton = {
                    TextButton(onClick = { showPermissionDialog = false }) {
                        Text("OK", color = primaryColor)
                    }
                }
            )
        }
    }
}