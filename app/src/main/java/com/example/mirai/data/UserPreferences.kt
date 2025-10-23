package com.example.mirai.data

import kotlinx.serialization.Serializable

/**
 * Data class representing user preferences and settings.
 *
 * @property userName The user's name (collected during onboarding)
 * @property isDarkTheme Whether dark theme is enabled (true) or light theme (false)
 * @property fontSize Font size setting: 0=small, 1=medium, 2=large
 *
 * Example usage:
 * ```
 * val preferences = UserPreferences(
 *     userName = "Alice",
 *     isDarkTheme = true,
 *     fontSize = 1
 * )
 * ```
 */
@Serializable
data class UserPreferences(
    val userName: String = "",
    val isDarkTheme: Boolean = false,
    val fontSize: Int = 1 // 0=small, 1=medium, 2=large
)

/**
 * Font size constants for easy reference in UI code.
 * Use these instead of magic numbers.
 */
object FontSize {
    const val SMALL = 0
    const val MEDIUM = 1
    const val LARGE = 2
}

/**
 * Helper function to get font size scale multiplier for UI.
 *
 * Example:
 * ```
 * val scale = getFontSizeScale(preferences.fontSize)
 * val actualSize = baseFontSize * scale // Apply to your text
 * ```
 */
fun getFontSizeScale(fontSize: Int): Float {
    return when (fontSize) {
        FontSize.SMALL -> 0.85f
        FontSize.MEDIUM -> 1.0f
        FontSize.LARGE -> 1.15f
        else -> 1.0f
    }
}