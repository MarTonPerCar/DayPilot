package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.AppSettings
import com.example.daypilot_test_desing.backend.repository.SettingsRepository

object FakeSettingsRepository : SettingsRepository {
    private var settings = AppSettings()

    override fun getSettings(): AppSettings = settings
    override fun toggleDarkMode(enabled: Boolean)      { settings = settings.copy(isDarkMode = enabled) }
    override fun selectTheme(themeId: String)          { settings = settings.copy(selectedThemeId = themeId) }
    override fun selectLanguage(language: String)      { settings = settings.copy(selectedLanguage = language) }
    override fun toggleNotifications(enabled: Boolean) { settings = settings.copy(notificationsEnabled = enabled) }
}
