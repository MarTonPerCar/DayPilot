package com.example.daypilot_test_desing.viewmodel.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import com.example.daypilot_test_desing.backend.fake.FakeUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences(application)

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun loadState() = SettingsUiState(
        name                 = FakeUserRepository.getCurrentUser().name,
        isDarkMode           = prefs.isDarkMode,
        selectedThemeId      = prefs.themeId,
        selectedLanguage     = prefs.language,
        notificationsEnabled = prefs.notificationsEnabled
    )

    fun toggleDarkMode(enabled: Boolean) {
        prefs.isDarkMode = enabled
        _uiState.value = loadState()
    }

    fun selectTheme(themeId: String) {
        prefs.themeId = themeId
        _uiState.value = loadState()
    }

    fun selectLanguage(language: String) {
        prefs.language = language
        _uiState.value = loadState()
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.notificationsEnabled = enabled
        _uiState.value = loadState()
    }
}
