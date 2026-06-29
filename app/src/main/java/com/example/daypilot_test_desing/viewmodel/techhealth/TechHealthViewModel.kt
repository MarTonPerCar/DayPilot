package com.example.daypilot_test_desing.viewmodel.techhealth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import com.example.daypilot_test_desing.backend.sharedprefs.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.backend.supabase.dto.HabitsDailyReadTechDto
import com.example.daypilot_test_desing.backend.supabase.dto.HabitsDailyTechDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertPointsLogDto
import com.example.daypilot_test_desing.backend.supabase.dto.TechHealthConfigDto
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

    init {
        viewModelScope.launch {
            loadFromSupabase()
            refreshUsageInternal()
        }
    }

    fun refreshUsage() {
        viewModelScope.launch { refreshUsageInternal() }
    }

    private suspend fun refreshUsageInternal() {
        val ctx = getApplication<Application>()
        val hasPermission = AppUsageTracker.hasPermission(ctx)
        if (hasPermission) {
            val usageMap = AppUsageTracker.getTodayUsage(ctx)
            repository.getAppRestrictions().forEach { r ->
                val used = usageMap[r.packageName] ?: 0
                if (used != r.usedMinutesToday) repository.updateUsage(r.id, used)
            }
        }
        scheduleNotificationsForOverLimit()
        val pointEarned = appPrefs.techHealthBonusDate == today() || readPointEarnedFromDb()
        if (!pointEarned) checkAndAwardDailyBonus()
        _uiState.value = TechHealthUiState(
            appRestrictions       = repository.getAppRestrictions(),
            groupRestrictions     = repository.getGroupRestrictions(),
            hasUsagePermission    = hasPermission,
            techHealthPointEarned = pointEarned || appPrefs.techHealthBonusDate == today()
        )
    }

    fun saveApp(restriction: AppRestriction) {
        repository.saveApp(restriction)
        viewModelScope.launch { upsertAppToSupabase(restriction) }
        scheduleNotificationsForOverLimit()
        _uiState.value = buildState()
    }

    fun saveGroup(restriction: GroupRestriction) {
        repository.saveGroup(restriction)
        _uiState.value = buildState()
    }

    fun toggleRestriction(id: String, enabled: Boolean) {
        val pkg = repository.getAppRestrictions().find { it.id == id }?.packageName
        repository.toggleRestriction(id, enabled)
        if (!enabled) TechHealthNotificationManager.cancel(getApplication(), id)
        else scheduleNotificationsForOverLimit()
        if (pkg != null) viewModelScope.launch { updateIsActiveInSupabase(pkg, enabled) }
        _uiState.value = buildState()
    }

    fun deleteRestriction(id: String) {
        TechHealthNotificationManager.cancel(getApplication(), id)
        val pkg = repository.getAppRestrictions().find { it.id == id }?.packageName
        repository.deleteRestriction(id)
        if (pkg != null) viewModelScope.launch { deleteFromSupabase(pkg) }
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

    // ── Supabase ops ─────────────────────────────────────────────────────────

    private suspend fun loadFromSupabase() {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            val configs = supabase.from("tech_health_config").select {
                filter {
                    eq("user_id", uid)
                    eq("is_active", true)
                }
            }.decodeList<TechHealthConfigDto>()
            configs.forEach { dto ->
                val limitMinutes = (dto.limitHours * 60).toInt()
                val existing = repository.getAppRestrictions().find { it.packageName == dto.appPackage }
                repository.saveApp(AppRestriction(
                    id                        = existing?.id ?: dto.appPackage,
                    appName                   = dto.appName,
                    packageName               = dto.appPackage,
                    dailyLimitMinutes         = limitMinutes,
                    notificationIntervalSeconds = existing?.notificationIntervalSeconds ?: 3600,
                    isEnabled                 = dto.isActive,
                    usedMinutesToday          = existing?.usedMinutesToday ?: 0
                ))
            }
        } catch (_: Exception) { }
        _uiState.value = buildState()
    }

    private suspend fun upsertAppToSupabase(restriction: AppRestriction) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_config").upsert(TechHealthConfigDto(
                userId     = uid,
                appPackage = restriction.packageName,
                appName    = restriction.appName,
                limitHours = restriction.dailyLimitMinutes / 60.0,
                isActive   = restriction.isEnabled
            ))
        } catch (_: Exception) { }
    }

    private suspend fun deleteFromSupabase(packageName: String) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_config").delete {
                filter {
                    eq("user_id", uid)
                    eq("app_package", packageName)
                }
            }
        } catch (_: Exception) { }
    }

    private suspend fun updateIsActiveInSupabase(packageName: String, isActive: Boolean) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_config").update({
                set("is_active", isActive)
            }) {
                filter {
                    eq("user_id", uid)
                    eq("app_package", packageName)
                }
            }
        } catch (_: Exception) { }
    }

    private suspend fun readPointEarnedFromDb(): Boolean {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return false
        return try {
            supabase.from("habits_daily").select {
                filter { eq("user_id", uid); eq("date", today()) }
                limit(1)
            }.decodeList<HabitsDailyReadTechDto>()
                .firstOrNull()?.techHealthPointEarned ?: false
        } catch (_: Exception) { false }
    }

    // ── Point logic ──────────────────────────────────────────────────────────

    private fun checkAndAwardDailyBonus() {
        val todayStr = today()
        if (appPrefs.techHealthBonusDate == todayStr) return
        val active = repository.getAppRestrictions().filter { it.isEnabled }
        if (active.isEmpty()) return
        if (active.all { it.usedMinutesToday < it.dailyLimitMinutes }) {
            appPrefs.techHealthBonusDate = todayStr
            viewModelScope.launch {
                logPointsToDb(10, "TECH_HEALTH")
                writeTechHealthEarned(true)
            }
        }
    }

    private suspend fun writeTechHealthEarned(earned: Boolean) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("habits_daily").upsert(
                HabitsDailyTechDto(userId = uid, date = today(), techHealthPointEarned = earned)
            )
        } catch (_: Exception) { }
    }

    private suspend fun logPointsToDb(points: Int, source: String) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("points_log").insert(
                InsertPointsLogDto(userId = uid, points = points, source = source, dayKey = today())
            )
        } catch (_: Exception) { }
    }

    // ── Notifications ────────────────────────────────────────────────────────

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

    private fun buildState() = TechHealthUiState(
        appRestrictions       = repository.getAppRestrictions(),
        groupRestrictions     = repository.getGroupRestrictions(),
        hasUsagePermission    = AppUsageTracker.hasPermission(getApplication()),
        techHealthPointEarned = appPrefs.techHealthBonusDate == today()
    )

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
