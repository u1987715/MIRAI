package com.example.mirai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Primary styled button component for Mirai app.
 * Full width by default with a maximum width of 400dp for larger screens.
 *
 * @param text The button text to display
 * @param onClick Callback when button is clicked
 * @param modifier Optional modifier for customization
 * @param enabled Whether the button is enabled (default: true)
 *
 * Example usage:
 * ```
 * PrimaryButton(
 *     text = "Continue",
 *     onClick = { navController.navigate(Screen.Main.route) },
 *     enabled = name.isNotBlank()
 * )
 * ```
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 400.dp), // Max width for tablets
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

/**
 * Preview on phone
 */
@Preview(
    name = "Phone - Enabled",
    widthDp = 360,
    showBackground = true
)
@Composable
fun PreviewPrimaryButtonPhone() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(
                text = "Continue",
                onClick = {}
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
fun PreviewPrimaryButtonTablet() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            PrimaryButton(
                text = "Get Started",
                onClick = {}
            )
        }
    }
}

/**
 * Preview disabled state
 */
@Preview(
    name = "Disabled",
    widthDp = 360,
    showBackground = true
)
@Composable
fun PreviewPrimaryButtonDisabled() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            PrimaryButton(
                text = "Continue",
                onClick = {},
                enabled = false
            )
        }
    }
}