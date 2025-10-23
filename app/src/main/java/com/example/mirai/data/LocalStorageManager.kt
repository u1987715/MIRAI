package com.example.mirai.data

import android.content.Context
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.IOException

/**
 * Manages local storage for diary entries and user preferences using JSON files.
 * This class handles all reading and writing of data to the device's internal storage.
 *
 * Usage:
 * ```
 * val storageManager = LocalStorageManager(context)
 *
 * // Save an entry
 * val entry = createDiaryEntry("Title", "Content")
 * storageManager.createEntry(entry)
 *
 * // Get all entries
 * val entries = storageManager.getAllEntries()
 * ```
 *
 * @property context Android Context used to access internal storage
 */
class LocalStorageManager(private val context: Context) {

    // JSON configuration with pretty printing for easy debugging
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true // Ignore unknown fields when deserializing
        encodeDefaults = true // Include default values in JSON
    }

    // File names for storage
    private val entriesFile = File(context.filesDir, "entries.json")
    private val preferencesFile = File(context.filesDir, "preferences.json")

    companion object {
        private const val TAG = "LocalStorageManager"
    }

    // ========== DIARY ENTRY OPERATIONS ==========

    /**
     * Creates a new diary entry and saves it to storage.
     * The entry is added to the existing list of entries.
     *
     * @param entry The diary entry to create
     *
     * Example:
     * ```
     * val newEntry = createDiaryEntry("My Day", "It was great!")
     * storageManager.createEntry(newEntry)
     * ```
     */
    suspend fun createEntry(entry: DiaryEntry) {
        try {
            // Get existing entries
            val entries = getAllEntries().toMutableList()

            // Add the new entry
            entries.add(entry)

            // Save back to file
            saveEntriesToFile(entries)

            Log.d(TAG, "Entry created successfully: ${entry.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error creating entry", e)
            throw e
        }
    }

    /**
     * Updates an existing diary entry.
     * Finds the entry by ID and replaces it with the updated version.
     *
     * @param entry The updated diary entry (must have matching ID)
     *
     * Example:
     * ```
     * val updatedEntry = existingEntry.copy(title = "New Title")
     * storageManager.updateEntry(updatedEntry)
     * ```
     */
    suspend fun updateEntry(entry: DiaryEntry) {
        try {
            val entries = getAllEntries().toMutableList()

            // Find the index of the entry to update
            val index = entries.indexOfFirst { it.id == entry.id }

            if (index != -1) {
                // Replace the old entry with the updated one
                entries[index] = entry
                saveEntriesToFile(entries)
                Log.d(TAG, "Entry updated successfully: ${entry.id}")
            } else {
                Log.w(TAG, "Entry not found for update: ${entry.id}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating entry", e)
            throw e
        }
    }

    /**
     * Deletes a diary entry by its ID.
     *
     * @param entryId The unique ID of the entry to delete
     *
     * Example:
     * ```
     * storageManager.deleteEntry("abc-123-def-456")
     * ```
     */
    suspend fun deleteEntry(entryId: String) {
        try {
            val entries = getAllEntries().toMutableList()

            // Remove the entry with matching ID
            val removed = entries.removeIf { it.id == entryId }

            if (removed) {
                saveEntriesToFile(entries)
                Log.d(TAG, "Entry deleted successfully: $entryId")
            } else {
                Log.w(TAG, "Entry not found for deletion: $entryId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting entry", e)
            throw e
        }
    }

    /**
     * Retrieves all diary entries, sorted by creation date (newest first).
     *
     * @return List of all diary entries, sorted by createdAt descending
     *
     * Example:
     * ```
     * val allEntries = storageManager.getAllEntries()
     * allEntries.forEach { entry ->
     *     println("${entry.title}: ${entry.content}")
     * }
     * ```
     */
    suspend fun getAllEntries(): List<DiaryEntry> {
        return try {
            if (!entriesFile.exists()) {
                // If file doesn't exist yet, return empty list
                Log.d(TAG, "Entries file doesn't exist, returning empty list")
                return emptyList()
            }

            // Read the JSON file
            val jsonString = entriesFile.readText()

            // Deserialize from JSON to List<DiaryEntry>
            val entries = json.decodeFromString<List<DiaryEntry>>(jsonString)

            // Sort by creation date, newest first
            entries.sortedByDescending { it.createdAt }

        } catch (e: Exception) {
            Log.e(TAG, "Error reading entries", e)
            emptyList() // Return empty list on error
        }
    }

    /**
     * Retrieves diary entries for a specific date.
     *
     * @param year The year (e.g., 2025)
     * @param month The month (1-12)
     * @param day The day of the month (1-31)
     * @return List of entries created on the specified date
     *
     * Example:
     * ```
     * // Get entries from October 23, 2025
     * val entries = storageManager.getEntriesByDate(2025, 10, 23)
     * ```
     */
    suspend fun getEntriesByDate(year: Int, month: Int, day: Int): List<DiaryEntry> {
        return try {
            val allEntries = getAllEntries()

            // Filter entries that match the specified date
            allEntries.filter { entry ->
                val calendar = java.util.Calendar.getInstance()
                calendar.timeInMillis = entry.createdAt

                calendar.get(java.util.Calendar.YEAR) == year &&
                        calendar.get(java.util.Calendar.MONTH) + 1 == month && // Month is 0-indexed
                        calendar.get(java.util.Calendar.DAY_OF_MONTH) == day
            }.sortedByDescending { it.createdAt }

        } catch (e: Exception) {
            Log.e(TAG, "Error getting entries by date", e)
            emptyList()
        }
    }

    /**
     * Internal helper function to save entries list to file.
     */
    private fun saveEntriesToFile(entries: List<DiaryEntry>) {
        try {
            // Serialize the list to JSON
            val jsonString = json.encodeToString(entries)

            // Write to file
            entriesFile.writeText(jsonString)

        } catch (e: IOException) {
            Log.e(TAG, "Error writing entries to file", e)
            throw e
        }
    }

    // ========== USER PREFERENCES OPERATIONS ==========

    /**
     * Saves user preferences to storage.
     *
     * @param preferences The user preferences to save
     *
     * Example:
     * ```
     * val prefs = UserPreferences(
     *     userName = "John",
     *     isDarkTheme = true,
     *     fontSize = 2
     * )
     * storageManager.savePreferences(prefs)
     * ```
     */
    suspend fun savePreferences(preferences: UserPreferences) {
        try {
            // Serialize preferences to JSON
            val jsonString = json.encodeToString(preferences)

            // Write to file
            preferencesFile.writeText(jsonString)

            Log.d(TAG, "Preferences saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving preferences", e)
            throw e
        }
    }

    /**
     * Retrieves user preferences from storage.
     * Returns default preferences if file doesn't exist.
     *
     * @return User preferences (or defaults if not set yet)
     *
     * Example:
     * ```
     * val prefs = storageManager.getPreferences()
     * println("User: ${prefs.userName}")
     * println("Dark theme: ${prefs.isDarkTheme}")
     * ```
     */
    suspend fun getPreferences(): UserPreferences {
        return try {
            if (!preferencesFile.exists()) {
                // If file doesn't exist, return default preferences
                Log.d(TAG, "Preferences file doesn't exist, returning defaults")
                return UserPreferences()
            }

            // Read and deserialize JSON
            val jsonString = preferencesFile.readText()
            json.decodeFromString<UserPreferences>(jsonString)

        } catch (e: Exception) {
            Log.e(TAG, "Error reading preferences", e)
            UserPreferences() // Return defaults on error
        }
    }

    /**
     * Clears all data (useful for testing or reset functionality).
     * WARNING: This deletes all entries and resets preferences!
     *
     * Example:
     * ```
     * storageManager.clearAllData() // Use with caution!
     * ```
     */
    suspend fun clearAllData() {
        try {
            if (entriesFile.exists()) {
                entriesFile.delete()
                Log.d(TAG, "Entries file deleted")
            }
            if (preferencesFile.exists()) {
                preferencesFile.delete()
                Log.d(TAG, "Preferences file deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing data", e)
            throw e
        }
    }
}