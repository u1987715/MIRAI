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

        // Pantalla principal (Home)
        composable("home") {
            HomeScreen(
                navController = navController,
                storageManager = storageManager,
                userName = "User", // de momento usamos uno fijo
                onNavigateToDetail = { /* TODO */ },
                onNavigateToCreate = { /* TODO */ },
                onNavigateToCalendar = { /* TODO */ },
                onNavigateToSettings = { /* TODO */ }
            )
        }
    }
}
