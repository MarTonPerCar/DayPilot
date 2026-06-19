package com.example.daypilot_test_desing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daypilot_test_desing.navigation.DayPilotNavGraph
import com.example.daypilot_test_desing.presentation.settings.SettingsViewModel
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
