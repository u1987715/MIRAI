package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext


/**
 * CalendarScreen.kt
 * Permite navegar por meses y ver entradas por fecha.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    storageManager: LocalStorageManager
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var entriesForSelectedDay by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Cargar entradas cuando cambie la fecha seleccionada
    LaunchedEffect(selectedDate) {
        selectedDate?.let { date ->
            scope.launch {
                try {
                    val all = storageManager.getAllEntries()
                    val formatted = dayFormat.format(date)
                    entriesForSelectedDay = all.filter {
                        dayFormat.format(Date(it.createdAt)) == formatted
                    }
                } catch (e: Exception) {
                    Toast.makeText(ctx, "Error loading entries", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //Encabezado con mes y flechas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    val newMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, -1)
                    }
                    currentMonth = newMonth
                }) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous month")
                }

                Text(
                    text = monthFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = {
                    val newMonth = (currentMonth.clone() as Calendar).apply {
                        add(Calendar.MONTH, 1)
                    }
                    currentMonth = newMonth
                }) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next month")
                }
            }

            Spacer(Modifier.height(8.dp))

            //Días de la semana
            val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            //Cuadrícula de días
            val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
            val firstDayOfWeek = currentMonth.apply { set(Calendar.DAY_OF_MONTH, 1) }
                .get(Calendar.DAY_OF_WEEK) - 1

            val totalCells = firstDayOfWeek + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column {
                for (row in 0 until totalRows) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (col in 0..6) {
                            val dayIndex = row * 7 + col
                            val dayNumber = dayIndex - firstDayOfWeek + 1
                            if (dayNumber in 1..daysInMonth) {
                                val calendarDay = Calendar.getInstance().apply {
                                    time = currentMonth.time
                                    set(Calendar.DAY_OF_MONTH, dayNumber)
                                }
                                val date = calendarDay.time
                                val formatted = dayFormat.format(date)

                                // ¿Tiene entradas este día?
                                var hasEntries by remember(currentMonth) { mutableStateOf(false) }

                                LaunchedEffect(currentMonth) {
                                    scope.launch {
                                        val all = storageManager.getAllEntries()
                                        hasEntries = all.any {
                                            dayFormat.format(Date(it.createdAt)) == formatted
                                        }
                                    }
                                }


                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clickable { selectedDate = date },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = dayNumber.toString())
                                    if (hasEntries) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 4.dp)
                                                .size(6.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            //Lista de entradas del día seleccionado
            if (selectedDate != null) {
                Text(
                    text = "Entries for ${dayFormat.format(selectedDate!!)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(Modifier.height(8.dp))
                if (entriesForSelectedDay.isEmpty()) {
                    Text("No entries for this day.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(entriesForSelectedDay) { entry ->
                            DiaryEntryCard(entry = entry) {
                                navController.navigate("entryDetail/${entry.id}")
                            }
                        }
                    }
                }
            }
        }
    }
}
