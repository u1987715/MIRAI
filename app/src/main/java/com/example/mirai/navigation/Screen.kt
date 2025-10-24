package com.example.mirai.navigation

/**
 * Sealed class representing all navigation destinations in the Mirai app.
 * This provides type-safe navigation with compile-time checks.
 *
 * Usage example:
 * ```
 * navController.navigate(Screen.CreateEntry.route)
 * navController.navigate(Screen.EditEntry("entry-123").route)
 * ```
 */
sealed class Screen(val route: String) {

    /**
     * Onboarding/Welcome screen - shown on first launch or when user is logged out
     */
    object Onboarding : Screen("onboarding")

    /**
     * Main home screen showing list of diary entries
     */
    object Main : Screen("main")

    /**
     * Screen to create a new diary entry
     */
    object CreateEntry : Screen("createEntry")

    /**
     * Screen to edit an existing diary entry
     * @param entryId The ID of the entry to edit
     *
     * Example: Screen.EditEntry("abc-123").route = "editEntry/abc-123"
     */
    data class EditEntry(val entryId: String) : Screen("editEntry/$entryId") {
        companion object {
            // Base route with parameter placeholder for navigation graph
            const val route = "editEntry/{entryId}"

            /**
             * Helper to extract entryId from navigation arguments
             */
            fun fromRoute(entryId: String?): EditEntry? {
                return entryId?.let { EditEntry(it) }
            }
        }
    }

    /**
     * Calendar view to browse entries by date
     */
    object Calendar : Screen("calendar")

    /**
     * Settings screen for theme, font size, and user preferences
     */
    object Settings : Screen("settings")
}
