package com.example.mirai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.mirai.data.*
import com.example.mirai.ui.theme.MIRAITheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MIRAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Person 2 will replace this with NavGraph
                    // Placeholder for now
                }
            }
        }

        // 🔹 Run quick data-layer test
        lifecycleScope.launch {
            val storage = LocalStorageManager(this@MainActivity)

            // Create test entry
            val testEntry = createDiaryEntry(
                title = "Test Entry",
                content = "This is a test entry"
            )
            storage.createEntry(testEntry)

            // Fetch all entries
            val entries = storage.getAllEntries()
            Log.d("MIRAI_TEST", "Entries count: ${entries.size}")
            entries.forEach {
                Log.d("MIRAI_TEST", "Entry: ${it.title} (${it.createdAt})")
            }

            // Save and load preferences
            val prefs = UserPreferences(userName = "Test User", isDarkTheme = true)
            storage.savePreferences(prefs)

            val loadedPrefs = storage.getPreferences()
            Log.d("MIRAI_TEST", "Loaded preferences: ${loadedPrefs.userName}, darkTheme=${loadedPrefs.isDarkTheme}")
        }
    }
}
