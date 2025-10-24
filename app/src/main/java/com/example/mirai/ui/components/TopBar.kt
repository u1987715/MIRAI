package com.example.mirai.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

/**
 * Reusable top app bar component for Mirai app.
 * Supports optional back navigation and trailing action buttons.
 *
 * @param title The title text to display
 * @param onNavigationClick Optional callback for back button. If null, no back button is shown.
 * @param actions Optional trailing action buttons (e.g., settings, menu)
 * @param modifier Optional modifier for customization
 *
 * Example usage:
 * ```
 * // Simple title only
 * TopBar(title = "Home")
 *
 * // With back button
 * TopBar(
 *     title = "Settings",
 *     onNavigationClick = { navController.popBackStack() }
 * )
 *
 * // With back button and actions
 * TopBar(
 *     title = "Edit Entry",
 *     onNavigationClick = { navController.popBackStack() },
 *     actions = {
 *         IconButton(onClick = { /* save */ }) {
 *             Icon(Icons.Default.Save, "Save")
 *         }
 *     }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onNavigationClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            // Only show back button if onNavigationClick is provided
            if (onNavigationClick != null) {
                IconButton(onClick = onNavigationClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Navigate back"
                    )
                }
            }
        },
        actions = actions,
        modifier = modifier
    )
}

/**
 * Preview: Simple title only
 */
@Preview(showBackground = true)
@Composable
fun PreviewTopBarSimple() {
    MaterialTheme {
        TopBar(title = "Home")
    }
}

/**
 * Preview: With back button
 */
@Preview(showBackground = true)
@Composable
fun PreviewTopBarWithBack() {
    MaterialTheme {
        TopBar(
            title = "Settings",
            onNavigationClick = {}
        )
    }
}

/**
 * Preview: With back button and action
 */
@Preview(showBackground = true)
@Composable
fun PreviewTopBarWithActions() {
    MaterialTheme {
        TopBar(
            title = "Edit Entry",
            onNavigationClick = {},
            actions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings"
                    )
                }
            }
        )
    }
}