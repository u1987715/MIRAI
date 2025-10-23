package com.example.mirai.data

import kotlinx.serialization.Serializable
import java.util.UUID

/**
 * Data class representing a single diary entry.
 *
 * @property id Unique identifier for the entry (auto-generated UUID)
 * @property title The title of the diary entry
 * @property createdAt Timestamp when the entry was created (milliseconds since epoch)
 * @property content The main text content of the diary entry
 * @property imagePaths List of file paths for images attached to this entry
 * @property audioPaths List of file paths for audio recordings attached to this entry
 *
 * Example usage:
 * ```
 * val newEntry = DiaryEntry(
 *     id = UUID.randomUUID().toString(),
 *     title = "My First Day",
 *     createdAt = System.currentTimeMillis(),
 *     content = "Today was amazing!",
 *     imagePaths = listOf("/data/user/0/com.example.mirai/files/images/photo1.jpg"),
 *     audioPaths = emptyList()
 * )
 * ```
 */
@Serializable
data class DiaryEntry(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val createdAt: Long = System.currentTimeMillis(),
    val content: String,
    val imagePaths: List<String> = emptyList(),
    val audioPaths: List<String> = emptyList()
)

/**
 * Helper function to create a new diary entry with auto-generated ID and timestamp.
 * Use this when creating new entries to ensure proper defaults.
 *
 * Example:
 * ```
 * val entry = createDiaryEntry(
 *     title = "Beach Day",
 *     content = "Went to the beach today!"
 * )
 * ```
 */
fun createDiaryEntry(
    title: String,
    content: String,
    imagePaths: List<String> = emptyList(),
    audioPaths: List<String> = emptyList()
): DiaryEntry {
    return DiaryEntry(
        id = UUID.randomUUID().toString(),
        title = title,
        createdAt = System.currentTimeMillis(),
        content = content,
        imagePaths = imagePaths,
        audioPaths = audioPaths
    )
}