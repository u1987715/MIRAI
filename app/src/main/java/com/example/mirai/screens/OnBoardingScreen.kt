package com.example.mirai.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mirai.R
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.components.PrimaryButton
import com.example.mirai.ui.theme.MiraiPink
import com.example.mirai.ui.theme.MiraiPurple

/**
 * Onboarding screen con diseño mejorado:
 * - Fondo con degradado del logo
 * - Logo MIRAI integrado
 * - Paleta de colores rosa-púrpura
 * - 2 pantallas: Welcome y Explanation
 */
@Composable
fun OnboardingScreen(
    navController: NavController,
    onComplete: (String) -> Unit
) {
    var currentScreen by remember { mutableStateOf(0) }
    var userName by remember { mutableStateOf("") }

    // Fondo con degradado
    GradientBackground {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val padding = if (maxWidth > 600.dp) 48.dp else 24.dp
            val verticalSpacing = if (maxHeight > 700.dp) 24.dp else 16.dp

            when (currentScreen) {
                0 -> WelcomeScreen(
                    name = userName,
                    onNameChange = { userName = it },
                    onContinue = { currentScreen = 1 },
                    padding = padding,
                    verticalSpacing = verticalSpacing
                )
                1 -> ExplanationScreen(
                    userName = userName,
                    onGetStarted = { onComplete(userName) },
                    padding = padding,
                    verticalSpacing = verticalSpacing
                )
            }
        }
    }
}

/**
 * Screen 1: Welcome con logo
 */
@Composable
private fun WelcomeScreen(
    name: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    padding: Dp,
    verticalSpacing: Dp
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo MIRAI
        // NOTA: Coloca tu logo en res/drawable/mirai_logo.png
        // Por ahora usaremos un placeholder
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Si tienes el logo en drawable, descomenta:
           Image(
                painter = painterResource(id = R.drawable.mirai_logo),
                contentDescription = "MIRAI Logo",
                modifier = Modifier.size(120.dp)
             )

            // Placeholder temporal (círculo con gradiente)
            Surface(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                color = MiraiPink
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨",
                        style = MaterialTheme.typography.displayLarge,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(verticalSpacing * 2))

        // Título
        Text(
            text = "Welcome to",
            style = MaterialTheme.typography.titleLarge,
            color = MiraiPink,
            textAlign = TextAlign.Center
        )

        Text(
            text = "MIRAI",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 56.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(verticalSpacing))

        Text(
            text = "Your personal diary",
            style = MaterialTheme.typography.bodyLarge,
            color = MiraiPurple.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(verticalSpacing * 3))

        // Campo de nombre con estilo mejorado
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {
                Text(
                    "What's your name?",
                    color = MiraiPink
                )
            },
            placeholder = {
                Text(
                    "Enter your name",
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MiraiPink,
                unfocusedBorderColor = MiraiPurple.copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = MiraiPink
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    if (name.isNotBlank()) {
                        onContinue()
                    }
                }
            )
        )

        Spacer(modifier = Modifier.height(verticalSpacing * 2))

        // Botón Continue
        Button(
            onClick = {
                keyboardController?.hide()
                onContinue()
            },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiraiPink,
                contentColor = Color.White,
                disabledContainerColor = MiraiPurple.copy(alpha = 0.3f),
                disabledContentColor = Color.White.copy(alpha = 0.5f)
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Continue",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Screen 2: Explanation
 */
@Composable
private fun ExplanationScreen(
    userName: String,
    onGetStarted: () -> Unit,
    padding: Dp,
    verticalSpacing: Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Emoji decorativo
        Text(
            text = "✨📖✨",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(verticalSpacing))

        // Saludo personalizado
        Text(
            text = "Hi, $userName! 👋",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(verticalSpacing / 2))

        Text(
            text = "Your Personal Diary",
            style = MaterialTheme.typography.titleLarge,
            color = MiraiPink,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(verticalSpacing * 2))

        // Descripción
        Text(
            text = "MIRAI helps you capture your thoughts, memories, and moments. " +
                    "Write entries, attach photos, and revisit your past anytime.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(verticalSpacing * 2))

        // Features con estilo mejorado
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.1f)
            ),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FeatureItem("✍️", "Write daily entries")
                FeatureItem("📷", "Attach photos and memories")
                FeatureItem("📅", "Browse by calendar")
                FeatureItem("🎨", "Customize your experience")
            }
        }

        Spacer(modifier = Modifier.height(verticalSpacing * 3))

        // Botón Get Started
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MiraiPurple,
                contentColor = Color.White
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Get Started ✨",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

/**
 * Item de feature mejorado
 */
@Composable
private fun FeatureItem(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.width(48.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Preview
 */
@Preview(showBackground = true)
@Composable
fun PreviewOnboardingScreen() {
    MaterialTheme {
        val navController = rememberNavController()
        OnboardingScreen(
            navController = navController,
            onComplete = {}
        )
    }
}