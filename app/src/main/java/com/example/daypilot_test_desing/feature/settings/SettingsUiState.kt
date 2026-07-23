package com.example.daypilot_test_desing.feature.settings

data class SettingsUiState(
    val name: String = "",
    val isDarkMode: Boolean = true,
    val selectedThemeId: String = "SAGE_GREEN",
    val selectedLanguage: String = "es",
    val notificationsEnabled: Boolean = true,
    val taskRemindersEnabled: Boolean = true,
    val streakAlertsEnabled: Boolean = true,
    val exactAlarmsGranted: Boolean = true,
    val batteryOptimizationExempt: Boolean = true
)
