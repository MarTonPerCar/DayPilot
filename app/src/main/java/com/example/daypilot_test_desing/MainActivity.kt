package com.example.daypilot_test_desing

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daypilot_test_desing.navigation.DayPilotNavGraph
import com.example.daypilot_test_desing.viewmodel.settings.SettingsViewModel
import com.example.daypilot_test_desing.reminders.createNotificationChannel
import com.example.daypilot_test_desing.reminders.createTechHealthChannel
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

class MainActivity : ComponentActivity() {

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        createTechHealthChannel(this)

        val toRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
        ) toRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
        ) toRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (toRequest.isNotEmpty()) requestPermissions.launch(toRequest.toTypedArray())
        setTheme(R.style.Theme_DayPilotTestDesing)
        enableEdgeToEdge()
        setContent {
            val settingsVM: SettingsViewModel = viewModel()
            val settings by settingsVM.uiState.collectAsState()

            val theme = DayPilotTheme.entries.find { it.name == settings.selectedThemeId }
                ?: DayPilotTheme.SAGE_GREEN
            val isDark = if (theme == DayPilotTheme.AMOLED) true else settings.isDarkMode

            DayPilotTheme(theme = theme, darkMode = isDark) {
                DayPilotNavGraph()
            }
        }
    }
}
