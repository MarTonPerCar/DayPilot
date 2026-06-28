package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.AppSettings

interface SettingsRepository {
    fun getSettings(): AppSettings
    fun toggleDarkMode(enabled: Boolean)
    fun selectTheme(themeId: String)
    fun selectLanguage(language: String)
    fun toggleNotifications(enabled: Boolean)
}
