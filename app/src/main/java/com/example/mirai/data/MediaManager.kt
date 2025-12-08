package com.example.mirai.data

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Manages media files with SDE-grade concurrency and safety.
 *
 * Changes:
 * - All operations are now `suspend` and run on [Dispatchers.IO].
 * - Prevents UI jank (ANRs) during file copies.
 */
class MediaManager {

    companion object {
        private const val TAG = "MediaManager"
        private const val IMAGES_DIR = "images"
        private const val AUDIO_DIR = "audio"
    }

    suspend fun saveImage(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val imagesDir = File(context.filesDir, IMAGES_DIR)
            if (!imagesDir.exists()) imagesDir.mkdirs()

            val fileName = "${UUID.randomUUID()}.jpg"
            val destinationFile = File(imagesDir, fileName)

            // Stream copy
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Could not open input stream for URI: $uri")

            Log.d(TAG, "Image saved: ${destinationFile.absolutePath}")
            destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            throw IOException("Failed to save image", e)
        }
    }

    suspend fun saveAudio(context: Context, uri: Uri): String = withContext(Dispatchers.IO) {
        try {
            val audioDir = File(context.filesDir, AUDIO_DIR)
            if (!audioDir.exists()) audioDir.mkdirs()

            val fileName = "${UUID.randomUUID()}.mp3"
            val destinationFile = File(audioDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            } ?: throw IOException("Could not open input stream")

            Log.d(TAG, "Audio saved: ${destinationFile.absolutePath}")
            destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error saving audio", e)
            throw IOException("Failed to save audio", e)
        }
    }

    suspend fun deleteMediaFile(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists() && file.delete()) {
                Log.d(TAG, "Deleted: $filePath")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting file", e)
            false
        }
    }

    suspend fun deleteEntryMedia(imagePaths: List<String>, audioPaths: List<String>) = withContext(Dispatchers.IO) {
        (imagePaths + audioPaths).forEach { path ->
            deleteMediaFile(path)
        }
    }
}