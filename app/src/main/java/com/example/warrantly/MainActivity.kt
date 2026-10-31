package com.example.warrantly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.warrantly.ui.navigation.WarrantlyNavGraph
import com.example.warrantly.ui.theme.WarrantlyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as WarrantlyApplication

        setContent {
            val themeMode by app.dataStoreManager.themeMode.collectAsStateWithLifecycle(initialValue = "SYSTEM")
            val darkTheme = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            WarrantlyTheme(darkTheme = darkTheme) {
                WarrantlyNavGraph(
                    productRepository = app.productRepository,
                    documentRepository = app.documentRepository,
                    dataStoreManager = app.dataStoreManager,
                    notificationScheduler = app.notificationScheduler,
                )
            }
        }
    }
}
