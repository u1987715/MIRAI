package com.example.mirai.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.screens.*
import kotlinx.coroutines.launch

/**
 * NavGraph con soporte para tema dinámico
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    storageManager: LocalStorageManager,
    startDestination: String = "welcome",
    isDarkTheme: Boolean = true,
    onThemeChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Cargar preferencias
    var userPreferences by remember { mutableStateOf<com.example.mirai.data.UserPreferences?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            try {
                userPreferences = storageManager.getPreferences()
            } catch (e: Exception) {
                userPreferences = com.example.mirai.data.UserPreferences()
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // ============================================
        // WELCOME SCREEN
        // ============================================
        composable(route = "welcome") {
            WelcomeScreen(
                navController = navController,
                storageManager = storageManager,
                homeRoute = "home",
                isDarkTheme = isDarkTheme
            )
        }

        // ============================================
        // HOME SCREEN
        // ============================================

        // Con userName
        composable(route = "home/{userName}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            HomeScreen(
                navController = navController,
                storageManager = storageManager,
                userName = userName,
                isDarkTheme = isDarkTheme,
                onNavigateToDetail = { entryId ->
                    navController.navigate("entryDetail/$entryId")
                },
                onNavigateToCreate = {
                    navController.navigate("createEditEntry")
                },
                onNavigateToCalendar = {
                    navController.navigate("calendar")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // Sin userName (fallback)
        composable(route = "home") {
            val userName = userPreferences?.userName ?: "User"
            HomeScreen(
                navController = navController,
                storageManager = storageManager,
                userName = userName,
                isDarkTheme = isDarkTheme,
                onNavigateToDetail = { entryId ->
                    navController.navigate("entryDetail/$entryId")
                },
                onNavigateToCreate = {
                    navController.navigate("createEditEntry")
                },
                onNavigateToCalendar = {
                    navController.navigate("calendar")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }

        // ============================================
        // ENTRY DETAIL
        // ============================================
        composable(
            route = "entryDetail/{entryId}",
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable

            EntryDetailScreen(
                navController = navController,
                storageManager = storageManager,
                entryId = entryId,
                onEdit = { id ->
                    navController.navigate("createEditEntry/$id")
                },
                onDeleteSuccess = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // ============================================
        // CREATE/EDIT ENTRY
        // ============================================

        // Con entryId (editar)
        composable(
            route = "createEditEntry/{entryId}",
            arguments = listOf(
                navArgument("entryId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            CreateEditEntryScreen(
                navController = navController,
                storageManager = storageManager,
                entryId = entryId,
                isDarkTheme = isDarkTheme
            )
        }

        // Sin entryId (crear)
        composable(route = "createEditEntry") {
            CreateEditEntryScreen(
                navController = navController,
                storageManager = storageManager,
                entryId = null,
                isDarkTheme = isDarkTheme
            )
        }

        // ============================================
        // CALENDAR
        // ============================================
        composable(route = "calendar") {
            CalendarScreen(
                navController = navController,
                storageManager = storageManager,
                isDarkTheme = isDarkTheme
            )
        }

        // ============================================
        // SETTINGS (con theme toggle)
        // ============================================
        composable(route = "settings") {
            SettingsScreen(
                navController = navController,
                storageManager = storageManager,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }
    }
}