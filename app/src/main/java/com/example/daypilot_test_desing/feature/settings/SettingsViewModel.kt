package com.example.daypilot_test_desing.feature.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.core.data.repository.UserRepository
import com.example.daypilot_test_desing.core.reminders.DailyNotificationScheduler
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

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = loadUserName()

    private fun loadPrefsState() = SettingsUiState(
        isDarkMode           = prefs.isDarkMode,
        selectedThemeId      = prefs.themeId,
        selectedLanguage     = prefs.language,
        notificationsEnabled = prefs.notificationsEnabled,
        taskRemindersEnabled = prefs.taskRemindersEnabled,
        streakAlertsEnabled  = prefs.streakAlertsEnabled
    )

    private suspend fun loadUserName(): Boolean {
        return try {
            val name = userRepo.getCurrentUser().name
            _uiState.value = _uiState.value.copy(name = name)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load user name for settings", e)
            false
        }
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
        // Master switch: cancel or re-schedule daily phone alarms
        if (enabled) {
            val taskOn   = prefs.taskRemindersEnabled
            val streakOn = prefs.streakAlertsEnabled
            DailyNotificationScheduler.scheduleTaskReminder(getApplication(), taskOn)
            DailyNotificationScheduler.scheduleStreakAlert(getApplication(), streakOn)
        } else {
            DailyNotificationScheduler.cancelAll(getApplication())
        }
    }

    fun toggleTaskReminders(enabled: Boolean) {
        prefs.taskRemindersEnabled = enabled
        _uiState.value = _uiState.value.copy(taskRemindersEnabled = enabled)
        if (prefs.notificationsEnabled) {
            DailyNotificationScheduler.scheduleTaskReminder(getApplication(), enabled)
        }
    }

    fun toggleStreakAlerts(enabled: Boolean) {
        prefs.streakAlertsEnabled = enabled
        _uiState.value = _uiState.value.copy(streakAlertsEnabled = enabled)
        if (prefs.notificationsEnabled) {
            DailyNotificationScheduler.scheduleStreakAlert(getApplication(), enabled)
        }
    }

    companion object {
        private const val TAG = "SettingsViewModel"

        fun factory(application: Application, userRepo: UserRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SettingsViewModel(application, userRepo) as T
            }
    }
}
