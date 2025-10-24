package com.example.mirai.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mirai.data.DiaryEntry
import java.text.SimpleDateFormat
import java.util.*

/**
 * Card component to display a diary entry in a list.
 * Shows title, content preview, and formatted date.
 * Responsive: increases padding on tablets (width > 600dp).
 *
 * @param entry The diary entry to display
 * @param onClick Callback when the card is clicked
 * @param modifier Optional modifier for customization
 *
 * Example usage:
 * ```
 * DiaryEntryCard(
 *     entry = myEntry,
 *     onClick = { navController.navigate("entryDetail/${myEntry.id}") }
 * )
 * ```
 */
@Composable
fun DiaryEntryCard(
    entry: DiaryEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use BoxWithConstraints to detect screen size and adjust padding
    BoxWithConstraints {
        // Tablet: width > 600dp -> larger padding
        val cardPadding = if (maxWidth > 600.dp) 20.dp else 16.dp

        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(cardPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title - bold, 18sp
                Text(
                    text = entry.title.ifBlank { "(Untitled)" },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Formatted date - e.g., "Jan 23, 2025"
                val formattedDate = remember(entry.createdAt) {
                    SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        .format(Date(entry.createdAt))
                }

                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Content preview - max 2 lines, gray text
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Preview for phone size
 */
@Preview(
    name = "Phone",
    widthDp = 360,
    heightDp = 640,
    showBackground = true
)
@Composable
fun PreviewDiaryEntryCardPhone() {
    MaterialTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "1",
                title = "My First Day",
                content = "Today was amazing! I learned so much about Kotlin and Jetpack Compose. Can't wait to continue building this app.",
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

/**
 * Preview for tablet size
 */
@Preview(
    name = "Tablet",
    widthDp = 800,
    heightDp = 1280,
    showBackground = true
)
@Composable
fun PreviewDiaryEntryCardTablet() {
    MaterialTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "2",
                title = "Beach Day Adventure",
                content = "Went to the beach with friends. The sunset was breathtaking. We took lots of photos and had a great time swimming.",
                createdAt = System.currentTimeMillis() - 86400000
            ),
            onClick = {}
        )
    }
}

/**
 * Preview with empty title
 */
@Preview(
    name = "Empty Title",
    widthDp = 360,
    showBackground = true
)
@Composable
fun PreviewDiaryEntryCardEmptyTitle() {
    MaterialTheme {
        DiaryEntryCard(
            entry = DiaryEntry(
                id = "3",
                title = "",
                content = "Just some quick thoughts without a title...",
                createdAt = System.currentTimeMillis()
            ),
            onClick = {}
        )
    }
}

// Helper function to remember formatted date (prevent recomputation)
@Composable
private fun remember(key: Any, calculation: () -> String): String {
    return androidx.compose.runtime.remember(key) { calculation() }
}