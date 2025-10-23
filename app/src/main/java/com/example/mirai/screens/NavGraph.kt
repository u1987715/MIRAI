package com.example.mirai.screens

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mirai.data.LocalStorageManager

/**
 * Define todas las pantallas navegables de la app MIRAI.
 * Controla las rutas Welcome → Home (y otras futuras).
 */
@Composable
fun AppNavGraph(
    navController: NavHostController,
    storageManager: LocalStorageManager
) {
    NavHost(
        navController = navController,
        startDestination = "welcome"  // pantalla inicial
    ) {
        // Pantalla de bienvenida
        composable("welcome") {
            WelcomeScreen(
                navController = navController,
                storageManager = storageManager,
                homeRoute = "home"
            )
        }

        // Pantalla principal (Home) con argumento
        composable("home/{userName}") { backStackEntry ->
            val userName = backStackEntry.arguments?.getString("userName") ?: "User"
            HomeScreen(
                navController = navController,
                storageManager = storageManager,
                userName = userName,
                onNavigateToDetail = { entryId ->
                    navController.navigate("entryDetail/$entryId")
                },
                onNavigateToCreate = {
                    navController.navigate("createEditEntry")
                },
                onNavigateToCalendar = { /* TODO */ },
                onNavigateToSettings = { /* TODO */ }
            )
        }
        // home sin argumentos
        composable("home") {
            HomeScreen(
                navController = navController,
                storageManager = storageManager,
                userName = "User",
                onNavigateToDetail = { id -> navController.navigate("entryDetail/$id") },
                onNavigateToCreate = { navController.navigate("createEditEntry") },
                onNavigateToCalendar = { /* TODO */ },
                onNavigateToSettings = { /* TODO */ }
            )
        }

        //pantalla de visualización de entrada
        composable("entryDetail/{entryId}") { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: return@composable

            EntryDetailScreen(
                navController = navController,
                storageManager = storageManager,
                entryId = entryId,
                onEdit = { id ->
                    // más adelante navegarás a la pantalla de edición
                    // navController.navigate("editEntry/$id")
                },
                onDeleteSuccess = {
                    navController.popBackStack("home", inclusive = false)
                }
            )
        }
        //pantalla de crear o editar una entrada
        composable("createEditEntry/{entryId}") { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId")
            CreateEditEntryScreen(
                navController = navController,
                storageManager = storageManager,
                entryId = entryId
            )
        }
        composable("createEditEntry") {
            CreateEditEntryScreen(
                navController = navController,
                storageManager = storageManager
            )
        }





    }
}
