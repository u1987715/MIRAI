package com.example.mirai.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Manages local storage with Thread Safety and Atomic Writes.
 *
 * SDE Note:
 * - Uses [Dispatchers.IO] to prevent UI freezes (ANRs).
 * - Uses [Mutex] to prevent Race Conditions during read-modify-write cycles.
 * - Uses Atomic Write pattern (write to temp -> rename) to prevent data corruption on crash.
 */
class LocalStorageManager(context: Context) {

    // Use applicationContext to prevent Activity leaks
    private val context = context.applicationContext

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true // Handle type mismatches gracefully
    }

    private val entriesFile = File(this.context.filesDir, "entries.json")
    private val preferencesFile = File(this.context.filesDir, "preferences.json")

    // Mutex ensures only one coroutine can access the file at a time
    private val fileMutex = Mutex()

    companion object {
        private const val TAG = "LocalStorageManager"
    }

    // ========== DIARY ENTRY OPERATIONS ==========

    suspend fun createEntry(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            try {
                val entries = readEntriesInternal().toMutableList()
                entries.add(entry)
                saveEntriesAtomic(entries)
                Log.d(TAG, "Entry created successfully: ${entry.id}")
            } catch (e: Exception) {
                Log.e(TAG, "Error creating entry", e)
                throw e
            }
        }
    }

    suspend fun updateEntry(entry: DiaryEntry) = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            try {
                val entries = readEntriesInternal().toMutableList()
                val index = entries.indexOfFirst { it.id == entry.id }

                if (index != -1) {
                    entries[index] = entry
                    saveEntriesAtomic(entries)
                    Log.d(TAG, "Entry updated successfully: ${entry.id}")
                } else {
                    Log.w(TAG, "Entry not found for update: ${entry.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating entry", e)
                throw e
            }
        }
    }

    suspend fun deleteEntry(entryId: String) = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            try {
                val entries = readEntriesInternal().toMutableList()
                val removed = entries.removeIf { it.id == entryId }

                if (removed) {
                    saveEntriesAtomic(entries)
                    Log.d(TAG, "Entry deleted successfully: $entryId")
                } else {
                    Log.w(TAG, "Entry not found for deletion: $entryId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting entry", e)
                throw e
            }
        }
    }

    suspend fun getAllEntries(): List<DiaryEntry> = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            // Sort publicly, but read raw internally
            readEntriesInternal().sortedByDescending { it.createdAt }
        }
    }

    suspend fun getEntriesByDate(year: Int, month: Int, day: Int): List<DiaryEntry> = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            val allEntries = readEntriesInternal()

            allEntries.filter { entry ->
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = entry.createdAt
                calendar.get(java.util.Calendar.YEAR) == year &&
                        calendar.get(java.util.Calendar.MONTH) + 1 == month &&
                        calendar.get(java.util.Calendar.DAY_OF_MONTH) == day
            }.sortedByDescending { it.createdAt }
        }
    }

    // ========== INTERNAL HELPERS (Must be called within Lock) ==========

    /**
     * Reads entries directly from file without locking (caller must lock).
     */
    private fun readEntriesInternal(): List<DiaryEntry> {
        return try {
            if (!entriesFile.exists()) return emptyList()

            val jsonString = entriesFile.readText()
            if (jsonString.isBlank()) return emptyList()

            json.decodeFromString<List<DiaryEntry>>(jsonString)
        } catch (e: Exception) {
            Log.e(TAG, "CRITICAL: Error reading entries file. Possible corruption.", e)
            // SDE Strategy: In production, you might copy the corrupt file to a "backup"
            // folder here before returning emptyList() so data isn't lost forever.
            emptyList()
        }
    }

    /**
     * Performs an Atomic Write.
     * 1. Write to temporary file.
     * 2. Rename temporary file to actual file.
     * This prevents data corruption if the app crashes during write.
     */
    private fun saveEntriesAtomic(entries: List<DiaryEntry>) {
        val tempFile = File(context.filesDir, "entries.tmp")
        try {
            val jsonString = json.encodeToString(entries)
            tempFile.writeText(jsonString)

            // Atomic rename
            if (tempFile.renameTo(entriesFile)) {
                // Success
            } else {
                // Fallback for some OS versions or file locks
                entriesFile.delete()
                if (!tempFile.renameTo(entriesFile)) {
                    throw IOException("Failed to rename temp file to entries file")
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Error writing entries to file", e)
            tempFile.delete() // Clean up garbage
            throw e
        }
    }

    // ========== USER PREFERENCES OPERATIONS ==========

    suspend fun savePreferences(preferences: UserPreferences) = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            try {
                val jsonString = json.encodeToString(preferences)
                preferencesFile.writeText(jsonString)
                Log.d(TAG, "Preferences saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving preferences", e)
                throw e
            }
        }
    }

    suspend fun getPreferences(): UserPreferences = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            try {
                if (!preferencesFile.exists()) {
                    return@withLock UserPreferences()
                }
                val jsonString = preferencesFile.readText()
                json.decodeFromString<UserPreferences>(jsonString)
            } catch (e: Exception) {
                Log.e(TAG, "Error reading preferences", e)
                UserPreferences()
            }
        }
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        fileMutex.withLock {
            try {
                if (entriesFile.exists()) entriesFile.delete()
                if (preferencesFile.exists()) preferencesFile.delete()
                Log.d(TAG, "All data cleared")
            } catch (e: Exception) {
                Log.e(TAG, "Error clearing data", e)
                throw e
            }
        }
    }
}