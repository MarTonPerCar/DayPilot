package com.example.daypilot_test_desing.viewmodel.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import com.example.daypilot_test_desing.backend.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    application: Application,
    private val userRepo: UserRepository
) : AndroidViewModel(application) {

    private val prefs = AppPreferences(application)

    private val _uiState = MutableStateFlow(loadPrefsState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { loadUserName() } }

    fun refresh(): Job = viewModelScope.launch { loadUserName() }

    private fun loadPrefsState() = SettingsUiState(
        isDarkMode           = prefs.isDarkMode,
        selectedThemeId      = prefs.themeId,
        selectedLanguage     = prefs.language,
        notificationsEnabled = prefs.notificationsEnabled
    )

    private suspend fun loadUserName() {
        try {
            val name = userRepo.getCurrentUser().name
            _uiState.value = _uiState.value.copy(name = name)
        } catch (_: Exception) { }
    }

    fun toggleDarkMode(enabled: Boolean) {
        prefs.isDarkMode = enabled
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    fun selectTheme(themeId: String) {
        prefs.themeId = themeId
        _uiState.value = _uiState.value.copy(selectedThemeId = themeId)
    }

    fun selectLanguage(language: String) {
        prefs.language = language
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.notificationsEnabled = enabled
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    companion object {
        fun factory(application: Application, userRepo: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(application, userRepo) as T
            }
    }
}
