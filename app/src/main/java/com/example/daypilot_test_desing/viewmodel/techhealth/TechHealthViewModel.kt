package com.example.daypilot_test_desing.viewmodel.techhealth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction
import com.example.daypilot_test_desing.backend.fake.FakeProgressRepository
import com.example.daypilot_test_desing.backend.fake.FakeTechHealthRepository
import com.example.daypilot_test_desing.reminders.AppUsageTracker
import com.example.daypilot_test_desing.reminders.TechHealthNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TechHealthViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<TechHealthUiState> = _uiState.asStateFlow()

    private fun buildState() = TechHealthUiState(
        appRestrictions   = FakeTechHealthRepository.getAppRestrictions(),
        groupRestrictions = FakeTechHealthRepository.getGroupRestrictions()
    )

    // Called from NavGraph when the TechHealth screen opens
    fun refreshUsage() {
        val context = getApplication<Application>()
        if (AppUsageTracker.hasPermission(context)) {
            val usageMap = AppUsageTracker.getTodayUsage(context)
            FakeTechHealthRepository.getAppRestrictions().forEach { r ->
                val used = usageMap[r.packageName] ?: 0
                if (used != r.usedMinutesToday) FakeTechHealthRepository.updateUsage(r.id, used)
            }
        }
        scheduleNotificationsForOverLimit()
        checkAndAwardDailyBonus()
        _uiState.value = buildState()
    }

    fun saveApp(restriction: AppRestriction) {
        FakeTechHealthRepository.saveApp(restriction)
        scheduleNotificationsForOverLimit()
        _uiState.value = buildState()
    }

    fun saveGroup(restriction: GroupRestriction) {
        FakeTechHealthRepository.saveGroup(restriction)
        _uiState.value = buildState()
    }

    fun toggleRestriction(id: String, enabled: Boolean) {
        FakeTechHealthRepository.toggleRestriction(id, enabled)
        if (!enabled) TechHealthNotificationManager.cancel(getApplication(), id)
        else scheduleNotificationsForOverLimit()
        _uiState.value = buildState()
    }

    fun deleteRestriction(id: String) {
        TechHealthNotificationManager.cancel(getApplication(), id)
        FakeTechHealthRepository.deleteRestriction(id)
        _uiState.value = buildState()
    }

    fun toggleGroup(id: String, enabled: Boolean) {
        FakeTechHealthRepository.toggleGroup(id, enabled)
        _uiState.value = buildState()
    }

    fun deleteGroup(id: String) {
        FakeTechHealthRepository.deleteGroup(id)
        _uiState.value = buildState()
    }

    // Schedules repeating alarms for every restriction that has exceeded its daily limit
    private fun scheduleNotificationsForOverLimit() {
        val context = getApplication<Application>()
        FakeTechHealthRepository.getAppRestrictions().forEach { r ->
            if (r.isEnabled && r.usedMinutesToday >= r.dailyLimitMinutes
                && r.notificationIntervalSeconds > 0
            ) {
                TechHealthNotificationManager.scheduleRepeating(
                    context,
                    r.id,
                    r.appName,
                    r.usedMinutesToday,
                    r.dailyLimitMinutes,
                    r.notificationIntervalSeconds
                )
            } else {
                TechHealthNotificationManager.cancel(context, r.id)
            }
        }
    }

    // +10 pts bonus when ALL restrictions are under their limit and at least 2 exist
    private fun checkAndAwardDailyBonus() {
        if (FakeProgressRepository.isTechHealthBonusAwarded()) return
        val all = FakeTechHealthRepository.getAppRestrictions() +
                  FakeTechHealthRepository.getGroupRestrictions().flatMap { it.apps }
        if (all.size >= 2 && all.all { it.usedMinutesToday < it.dailyLimitMinutes }) {
            FakeProgressRepository.addTechHealthPoints(10)
        }
    }
}
