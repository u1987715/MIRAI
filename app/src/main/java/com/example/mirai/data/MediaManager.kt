package com.example.mirai.data

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

/**
 * Manages media files (images and audio) for diary entries.
 * This class handles copying media from URIs to internal storage and organizing them.
 *
 * Files are stored in:
 * - Images: /data/data/com.example.mirai/files/images/
 * - Audio: /data/data/com.example.mirai/files/audio/
 *
 * Usage:
 * ```
 * val mediaManager = MediaManager()
 *
 * // Save an image
 * val imagePath = mediaManager.saveImage(context, imageUri)
 *
 * // Save audio
 * val audioPath = mediaManager.saveAudio(context, audioUri)
 *
 * // Delete when no longer needed
 * mediaManager.deleteMediaFile(imagePath)
 * ```
 */
class MediaManager {

    companion object {
        private const val TAG = "MediaManager"
        private const val IMAGES_DIR = "images"
        private const val AUDIO_DIR = "audio"
    }

    /**
     * Saves an image from a URI to internal storage.
     * Creates a unique filename using UUID to prevent conflicts.
     *
     * @param context Android Context to access internal storage
     * @param uri The URI of the image to save (e.g., from image picker)
     * @return The absolute file path where the image was saved
     * @throws IOException if saving fails
     *
     * Example:
     * ```
     * // After user picks an image
     * val imageUri: Uri = ... // From image picker
     * val mediaManager = MediaManager()
     * val savedPath = mediaManager.saveImage(context, imageUri)
     * // savedPath = "/data/data/com.example.mirai/files/images/abc-123.jpg"
     * ```
     */
    fun saveImage(context: Context, uri: Uri): String {
        return try {
            // Create images directory if it doesn't exist
            val imagesDir = File(context.filesDir, IMAGES_DIR)
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
                Log.d(TAG, "Created images directory: ${imagesDir.absolutePath}")
            }

            // Generate unique filename with .jpg extension
            val fileName = "${UUID.randomUUID()}.jpg"
            val destinationFile = File(imagesDir, fileName)

            // Copy the file from URI to internal storage
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(TAG, "Image saved successfully: ${destinationFile.absolutePath}")
            destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error saving image", e)
            throw IOException("Failed to save image: ${e.message}", e)
        }
    }

    /**
     * Saves an audio file from a URI to internal storage.
     * Creates a unique filename using UUID to prevent conflicts.
     *
     * @param context Android Context to access internal storage
     * @param uri The URI of the audio file to save (e.g., from audio recorder)
     * @return The absolute file path where the audio was saved
     * @throws IOException if saving fails
     *
     * Example:
     * ```
     * // After user records audio
     * val audioUri: Uri = ... // From audio recorder
     * val mediaManager = MediaManager()
     * val savedPath = mediaManager.saveAudio(context, audioUri)
     * // savedPath = "/data/data/com.example.mirai/files/audio/xyz-789.mp3"
     * ```
     */
    fun saveAudio(context: Context, uri: Uri): String {
        return try {
            // Create audio directory if it doesn't exist
            val audioDir = File(context.filesDir, AUDIO_DIR)
            if (!audioDir.exists()) {
                audioDir.mkdirs()
                Log.d(TAG, "Created audio directory: ${audioDir.absolutePath}")
            }

            // Generate unique filename with .mp3 extension
            // Note: You can change extension based on actual format if needed
            val fileName = "${UUID.randomUUID()}.mp3"
            val destinationFile = File(audioDir, fileName)

            // Copy the file from URI to internal storage
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(destinationFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.d(TAG, "Audio saved successfully: ${destinationFile.absolutePath}")
            destinationFile.absolutePath

        } catch (e: Exception) {
            Log.e(TAG, "Error saving audio", e)
            throw IOException("Failed to save audio: ${e.message}", e)
        }
    }

    /**
     * Deletes a media file from internal storage.
     * Safe to call even if file doesn't exist.
     *
     * @param filePath The absolute path to the file to delete
     * @return true if file was deleted, false if it didn't exist or couldn't be deleted
     *
     * Example:
     * ```
     * val mediaManager = MediaManager()
     * val deleted = mediaManager.deleteMediaFile("/data/data/.../images/abc-123.jpg")
     * if (deleted) {
     *     println("File deleted successfully")
     * }
     * ```
     */
    fun deleteMediaFile(filePath: String): Boolean {
        return try {
            val file = File(filePath)

            if (file.exists()) {
                val deleted = file.delete()
                if (deleted) {
                    Log.d(TAG, "Media file deleted: $filePath")
                } else {
                    Log.w(TAG, "Failed to delete media file: $filePath")
                }
                deleted
            } else {
                Log.w(TAG, "Media file doesn't exist: $filePath")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting media file", e)
            false
        }
    }

    /**
     * Deletes all media files associated with a diary entry.
     * Useful when deleting an entire entry.
     *
     * @param imagePaths List of image file paths to delete
     * @param audioPaths List of audio file paths to delete
     *
     * Example:
     * ```
     * val mediaManager = MediaManager()
     * mediaManager.deleteEntryMedia(
     *     imagePaths = entry.imagePaths,
     *     audioPaths = entry.audioPaths
     * )
     * ```
     */
    fun deleteEntryMedia(imagePaths: List<String>, audioPaths: List<String>) {
        try {
            // Delete all images
            imagePaths.forEach { path ->
                deleteMediaFile(path)
            }

            // Delete all audio files
            audioPaths.forEach { path ->
                deleteMediaFile(path)
            }

            Log.d(TAG, "Deleted ${imagePaths.size} images and ${audioPaths.size} audio files")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting entry media", e)
        }
    }

    /**
     * Gets the total size of all media files in bytes.
     * Useful for showing storage usage to the user.
     *
     * @param context Android Context to access internal storage
     * @return Total size in bytes
     *
     * Example:
     * ```
     * val mediaManager = MediaManager()
     * val sizeBytes = mediaManager.getTotalMediaSize(context)
     * val sizeMB = sizeBytes / (1024.0 * 1024.0)
     * println("Total media storage: ${"%.2f".format(sizeMB)} MB")
     * ```
     */
    fun getTotalMediaSize(context: Context): Long {
        return try {
            val imagesDir = File(context.filesDir, IMAGES_DIR)
            val audioDir = File(context.filesDir, AUDIO_DIR)

            var totalSize = 0L

            // Calculate images size
            if (imagesDir.exists()) {
                imagesDir.listFiles()?.forEach { file ->
                    totalSize += file.length()
                }
            }

            // Calculate audio size
            if (audioDir.exists()) {
                audioDir.listFiles()?.forEach { file ->
                    totalSize += file.length()
                }
            }

            Log.d(TAG, "Total media size: $totalSize bytes")
            totalSize

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating media size", e)
            0L
        }
    }

    /**
     * Checks if a media file exists at the given path.
     *
     * @param filePath The absolute path to check
     * @return true if file exists, false otherwise
     *
     * Example:
     * ```
     * val mediaManager = MediaManager()
     * if (mediaManager.mediaFileExists(imagePath)) {
     *     // Display the image
     * } else {
     *     // Show placeholder or error
     * }
     * ```
     */
    fun mediaFileExists(filePath: String): Boolean {
        return try {
            File(filePath).exists()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if media file exists", e)
            false
        }
    }

    /**
     * Cleans up orphaned media files (files not referenced by any entry).
     * WARNING: This should be used carefully! Make sure to pass all valid paths.
     *
     * @param context Android Context to access internal storage
     * @param validPaths Set of file paths that should be kept
     * @return Number of files deleted
     *
     * Example:
     * ```
     * val allEntries = storageManager.getAllEntries()
     * val validPaths = allEntries.flatMap {
     *     it.imagePaths + it.audioPaths
     * }.toSet()
     * val mediaManager = MediaManager()
     * val deletedCount = mediaManager.cleanupOrphanedFiles(context, validPaths)
     * println("Cleaned up $deletedCount orphaned files")
     * ```
     */
    fun cleanupOrphanedFiles(context: Context, validPaths: Set<String>): Int {
        return try {
            var deletedCount = 0

            val imagesDir = File(context.filesDir, IMAGES_DIR)
            val audioDir = File(context.filesDir, AUDIO_DIR)

            // Check images directory
            if (imagesDir.exists()) {
                imagesDir.listFiles()?.forEach { file ->
                    if (file.absolutePath !in validPaths) {
                        if (file.delete()) {
                            deletedCount++
                            Log.d(TAG, "Deleted orphaned file: ${file.absolutePath}")
                        }
                    }
                }
            }

            // Check audio directory
            if (audioDir.exists()) {
                audioDir.listFiles()?.forEach { file ->
                    if (file.absolutePath !in validPaths) {
                        if (file.delete()) {
                            deletedCount++
                            Log.d(TAG, "Deleted orphaned file: ${file.absolutePath}")
                        }
                    }
                }
            }

            Log.d(TAG, "Cleanup complete. Deleted $deletedCount orphaned files")
            deletedCount

        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup", e)
            0
        }
    }
}