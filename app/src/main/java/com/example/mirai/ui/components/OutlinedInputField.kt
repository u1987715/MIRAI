package com.example.mirai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Multiline outlined text field wrapper for diary content.
 * Minimum height of 200dp for comfortable writing.
 *
 * @param value The current text value
 * @param onValueChange Callback when text changes
 * @param label The label text to display
 * @param placeholder The placeholder text when field is empty
 * @param modifier Optional modifier for customization
 *
 * Example usage:
 * ```
 * var content by remember { mutableStateOf("") }
 * OutlinedInputField(
 *     value = content,
 *     onValueChange = { content = it },
 *     label = "Content",
 *     placeholder = "What happened today?"
 * )
 * ```
 */
@Composable
fun OutlinedInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp), // Minimum height for comfortable writing
        textStyle = MaterialTheme.typography.bodyLarge,
        maxLines = Int.MAX_VALUE, // Allow unlimited lines
        minLines = 8 // Show at least 8 lines
    )
}

/**
 * Preview with empty content
 */
@Preview(
    name = "Empty",
    widthDp = 360,
    showBackground = true
)
@Composable
fun PreviewOutlinedInputFieldEmpty() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedInputField(
                value = text,
                onValueChange = { text = it },
                label = "Content",
                placeholder = "What happened today?"
            )
        }
    }
}

/**
 * Preview with content
 */
@Preview(
    name = "With Content",
    widthDp = 360,
    showBackground = true
)
@Composable
fun PreviewOutlinedInputFieldWithContent() {
    MaterialTheme {
        var text by remember {
            mutableStateOf("Today was an amazing day! I learned so much about Kotlin and Jetpack Compose. The team is working really well together.")
        }
        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedInputField(
                value = text,
                onValueChange = { text = it },
                label = "Diary Entry",
                placeholder = "Write your thoughts here..."
            )
        }
    }
}

/**
 * Preview on tablet
 */
@Preview(
    name = "Tablet",
    widthDp = 800,
    showBackground = true
)
@Composable
fun PreviewOutlinedInputFieldTablet() {
    MaterialTheme {
        var text by remember { mutableStateOf("") }
        Column(modifier = Modifier.padding(32.dp)) {
            OutlinedInputField(
                value = text,
                onValueChange = { text = it },
                label = "Content",
                placeholder = "Share your story..."
            )
        }
    }
}