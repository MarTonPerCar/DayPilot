package com.example.daypilot_test_desing.viewmodel.techhealth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import com.example.daypilot_test_desing.backend.sharedprefs.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.backend.supabase.dto.InsertPointsLogDto
import com.example.daypilot_test_desing.backend.supabase.supabase
import com.example.daypilot_test_desing.reminders.AppUsageTracker
import com.example.daypilot_test_desing.reminders.TechHealthNotificationManager
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TechHealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SharedPrefsTechHealthRepository(application)
    private val appPrefs   = AppPreferences(application)

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<TechHealthUiState> = _uiState.asStateFlow()

    private fun buildState() = TechHealthUiState(
        appRestrictions   = repository.getAppRestrictions(),
        groupRestrictions = repository.getGroupRestrictions()
    )

    fun refreshUsage() {
        val context = getApplication<Application>()
        if (AppUsageTracker.hasPermission(context)) {
            val usageMap = AppUsageTracker.getTodayUsage(context)
            repository.getAppRestrictions().forEach { r ->
                val used = usageMap[r.packageName] ?: 0
                if (used != r.usedMinutesToday) repository.updateUsage(r.id, used)
            }
        }
        scheduleNotificationsForOverLimit()
        checkAndAwardDailyBonus()
        _uiState.value = buildState()
    }

    fun saveApp(restriction: AppRestriction) {
        repository.saveApp(restriction)
        scheduleNotificationsForOverLimit()
        _uiState.value = buildState()
    }

    fun saveGroup(restriction: GroupRestriction) {
        repository.saveGroup(restriction)
        _uiState.value = buildState()
    }

    fun toggleRestriction(id: String, enabled: Boolean) {
        repository.toggleRestriction(id, enabled)
        if (!enabled) TechHealthNotificationManager.cancel(getApplication(), id)
        else scheduleNotificationsForOverLimit()
        _uiState.value = buildState()
    }

    fun deleteRestriction(id: String) {
        TechHealthNotificationManager.cancel(getApplication(), id)
        repository.deleteRestriction(id)
        _uiState.value = buildState()
    }

    fun toggleGroup(id: String, enabled: Boolean) {
        repository.toggleGroup(id, enabled)
        _uiState.value = buildState()
    }

    fun deleteGroup(id: String) {
        repository.deleteGroup(id)
        _uiState.value = buildState()
    }

    // +10 pts when ALL restrictions are under their daily limit and at least 2 exist
    private fun checkAndAwardDailyBonus() {
        val today = today()
        if (appPrefs.techHealthBonusDate == today) return
        val all = repository.getAppRestrictions() +
                  repository.getGroupRestrictions().flatMap { it.apps }
        if (all.size >= 2 && all.all { it.usedMinutesToday < it.dailyLimitMinutes }) {
            appPrefs.techHealthBonusDate = today
            viewModelScope.launch { logPointsToDb(10, "TECH_HEALTH") }
        }
    }

    private suspend fun logPointsToDb(points: Int, source: String) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("points_log").insert(
                InsertPointsLogDto(userId = uid, points = points, source = source, dayKey = today())
            )
        } catch (_: Exception) { }
    }

    private fun scheduleNotificationsForOverLimit() {
        val context = getApplication<Application>()
        repository.getAppRestrictions().forEach { r ->
            if (r.isEnabled && r.usedMinutesToday >= r.dailyLimitMinutes
                && r.notificationIntervalSeconds > 0
            ) {
                TechHealthNotificationManager.scheduleRepeating(
                    context, r.id, r.appName, r.usedMinutesToday,
                    r.dailyLimitMinutes, r.notificationIntervalSeconds
                )
            } else {
                TechHealthNotificationManager.cancel(context, r.id)
            }
        }
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
