package com.example.mirai.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.mirai.data.DiaryEntry
import com.example.mirai.data.LocalStorageManager
import com.example.mirai.ui.components.GradientBackground
import com.example.mirai.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * CalendarScreen con soporte para Dark/Light Mode
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    storageManager: LocalStorageManager,
    isDarkTheme: Boolean = true
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    var entriesForSelectedDay by remember { mutableStateOf<List<DiaryEntry>>(emptyList()) }

    val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val dayFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    // Colores según el tema
    val primaryColor = if (isDarkTheme) MiraiPink else MiraiTeal
    val secondaryColor = if (isDarkTheme) MiraiPurple else MiraiGreen
    val textColor = if (isDarkTheme) Color.White else MiraiTextDark
    val cardColor = if (isDarkTheme) {
        Color(0xFF2D1B3D).copy(alpha = 0.6f)
    } else {
        Color(0xFFE8F5EE).copy(alpha = 0.8f)
    }

    // Fondo con degradado según tema
    GradientBackground(darkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Calendar",
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = primaryColor
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header del mes con navegación
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, -1)
                        }
                    }) {
                        Icon(
                            Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Previous month",
                            tint = primaryColor
                        )
                    }

                    Text(
                        text = monthFormat.format(currentMonth.time),
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = {
                        currentMonth = (currentMonth.clone() as Calendar).apply {
                            add(Calendar.MONTH, 1)
                        }
                    }) {
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next month",
                            tint = primaryColor
                        )
                    }
                }

                // Grid del calendario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = cardColor
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        // Días de la semana
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Text(
                                    text = day,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Center,
                                    color = secondaryColor,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // Días del mes
                        val cal = currentMonth.clone() as Calendar
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
                        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                        val weeks = (firstDayOfWeek + daysInMonth + 6) / 7

                        for (week in 0 until weeks) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                for (dayOfWeek in 0..6) {
                                    val dayIndex = week * 7 + dayOfWeek - firstDayOfWeek + 1
                                    if (dayIndex in 1..daysInMonth) {
                                        val dayDate = (currentMonth.clone() as Calendar).apply {
                                            set(Calendar.DAY_OF_MONTH, dayIndex)
                                        }.time

                                        val isSelected = selectedDate?.let {
                                            dayFormat.format(it) == dayFormat.format(dayDate)
                                        } ?: false

                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .padding(4.dp)
                                                .background(
                                                    color = if (isSelected) primaryColor else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable {
                                                    selectedDate = dayDate
                                                    scope.launch {
                                                        try {
                                                            val allEntries = storageManager.getAllEntries()
                                                            entriesForSelectedDay = allEntries.filter {
                                                                dayFormat.format(Date(it.createdAt)) ==
                                                                        dayFormat.format(dayDate)
                                                            }
                                                        } catch (e: Exception) {
                                                            Toast
                                                                .makeText(
                                                                    ctx,
                                                                    "Error loading entries",
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                        }
                                                    }
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = dayIndex.toString(),
                                                color = if (isSelected) Color.White else textColor,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Lista de entradas del día seleccionado
                if (selectedDate != null) {
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "Entries for ${dayFormat.format(selectedDate!!)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold
                    )

                    if (entriesForSelectedDay.isEmpty()) {
                        Text(
                            text = "No entries for this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.7f)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(entriesForSelectedDay) { entry ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            navController.navigate("entryDetail/${entry.id}")
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = cardColor
                                    )
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = entry.title.ifBlank { "(Untitled)" },
                                            style = MaterialTheme.typography.titleSmall,
                                            color = textColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (entry.content.isNotBlank()) {
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                text = entry.content.take(100),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = textColor.copy(alpha = 0.8f),
                                                maxLines = 2
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}