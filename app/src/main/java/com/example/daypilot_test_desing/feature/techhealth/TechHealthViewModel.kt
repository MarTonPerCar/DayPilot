package com.example.daypilot_test_desing.feature.techhealth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction
import com.example.daypilot_test_desing.core.data.local.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyReadTechDto
import com.example.daypilot_test_desing.data.supabase.dto.TechHealthConfigDto
import com.example.daypilot_test_desing.data.supabase.supabase
import com.example.daypilot_test_desing.core.reminders.AppUsageTracker
import com.example.daypilot_test_desing.core.reminders.DayPilotAccessibilityService
import com.example.daypilot_test_desing.core.reminders.TechHealthNotificationManager
import androidx.core.app.NotificationManagerCompat
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

    // true while today has no violation; the bonus itself is awarded server-side
    private var cachedPointEarned = false

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

    /** Call on logout to prevent the next user seeing this user's cached restrictions. */
    fun clearLocalData() {
        repository.clearAll()
        cachedPointEarned = false
        _uiState.value = buildState()
    }

    private suspend fun refreshUsageInternal() {
        val ctx = getApplication<Application>()
        // worker also does this, but it can be delayed hours by Doze
        repository.applyPendingChangesIfNewDay()
        val hasPermission = AppUsageTracker.hasPermission(ctx)
        if (hasPermission) {
            val usageMap = AppUsageTracker.getTodayUsage(ctx)
            repository.getAppRestrictions().forEach { r ->
                val used = usageMap[r.packageName] ?: 0
                if (used != r.usedMinutesToday) repository.updateUsage(r.id, used)
            }
        }
        cancelOverLimitNotifications()
        cachedPointEarned = readPointEarnedFromDb()
        _uiState.value = TechHealthUiState(
            appRestrictions       = repository.getAppRestrictions(),
            groupRestrictions     = repository.getGroupRestrictions(),
            hasUsagePermission    = hasPermission,
            hasAccessibilityPermission = DayPilotAccessibilityService.isEnabled(ctx),
            techHealthPointEarned = cachedPointEarned,
            activeRestrictionCount = repository.getAppRestrictions().count { it.isEnabled }
        )
    }

    fun saveApp(restriction: AppRestriction) {
        repository.saveApp(restriction)
        viewModelScope.launch { upsertAppToSupabase(restriction) }
        cancelOverLimitNotifications()
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
        else cancelOverLimitNotifications()
        if (pkg != null) viewModelScope.launch { updateIsActiveInSupabase(pkg, enabled) }
        _uiState.value = buildState()
    }

    fun deleteRestriction(id: String) {
        TechHealthNotificationManager.cancel(getApplication(), id)
        val pkg = repository.getAppRestrictions().find { it.id == id }?.packageName
        repository.deleteRestriction(id)
        if (pkg != null) viewModelScope.launch { markPendingDeleteInSupabase(pkg) }
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

            // REPLACE local data — never merge, so the wrong-user bug can't occur.
            repository.clearAll()
            configs.forEach { dto ->
                val limitMinutes = (dto.limitHours * 60).toInt()
                repository.saveApp(AppRestriction(
                    id                          = dto.appPackage,
                    appName                     = dto.appName,
                    packageName                 = dto.appPackage,
                    dailyLimitMinutes           = limitMinutes,
                    notificationIntervalSeconds = 3600,
                    isEnabled                   = dto.isActive,
                    usedMinutesToday            = 0,
                    pendingDelete               = dto.pendingDelete
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
            // upsert skips false defaults (encodeDefaults=false), so clear it explicitly
            supabase.from("tech_health_config").update({
                set("pending_delete", false)
            }) {
                filter {
                    eq("user_id", uid)
                    eq("app_package", restriction.packageName)
                }
            }
        } catch (_: Exception) { }
    }

    // soft delete, removed for real by fn_close_daily_progress() that night
    private suspend fun markPendingDeleteInSupabase(packageName: String) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_config").update({
                set("pending_delete", true)
            }) {
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
        val uid = supabase.auth.currentUserOrNull()?.id ?: return true
        return try {
            supabase.from("habits_daily").select {
                filter { eq("user_id", uid); eq("date", today()) }
                limit(1)
            }.decodeList<HabitsDailyReadTechDto>()
                .firstOrNull()?.techHealthPointEarned ?: true
        } catch (_: Exception) { true }
    }

    // ── Notifications ────────────────────────────────────────────────────────

    private fun cancelOverLimitNotifications() {
        val context = getApplication<Application>()
        repository.getAppRestrictions().forEach { r ->
            TechHealthNotificationManager.cancel(context, r.id)
            NotificationManagerCompat.from(context).cancel(r.appName.hashCode())
        }
    }

    private fun buildState(): TechHealthUiState {
        val apps = repository.getAppRestrictions()
        return TechHealthUiState(
            appRestrictions        = apps,
            groupRestrictions      = repository.getGroupRestrictions(),
            hasUsagePermission     = AppUsageTracker.hasPermission(getApplication()),
            hasAccessibilityPermission = DayPilotAccessibilityService.isEnabled(getApplication()),
            techHealthPointEarned  = cachedPointEarned,
            activeRestrictionCount = apps.count { it.isEnabled }
        )
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
