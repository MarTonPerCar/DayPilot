package com.example.daypilot_test_desing.presentation.settings

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeSettingsRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private fun buildState(): SettingsUiState {
        val s = FakeSettingsRepository.getSettings()
        return SettingsUiState(
            name                 = FakeUserRepository.getCurrentUser().name,
            isDarkMode           = s.isDarkMode,
            selectedThemeId      = s.selectedThemeId,
            selectedLanguage     = s.selectedLanguage,
            notificationsEnabled = s.notificationsEnabled
        )
    }

    fun toggleDarkMode(enabled: Boolean) {
        FakeSettingsRepository.toggleDarkMode(enabled)
        _uiState.value = buildState()
    }

    fun selectTheme(themeId: String) {
        FakeSettingsRepository.selectTheme(themeId)
        _uiState.value = buildState()
    }

    fun selectLanguage(language: String) {
        FakeSettingsRepository.selectLanguage(language)
        _uiState.value = buildState()
    }

    fun toggleNotifications(enabled: Boolean) {
        FakeSettingsRepository.toggleNotifications(enabled)
        _uiState.value = buildState()
    }
}
