package com.example.daypilot_test_desing.presentation.settings

data class SettingsUiState(
    val name: String = "",
    val isDarkMode: Boolean = true,
    val selectedThemeId: String = "SAGE_GREEN",
    val selectedLanguage: String = "es",
    val notificationsEnabled: Boolean = true
)
