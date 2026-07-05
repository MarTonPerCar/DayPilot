package com.example.daypilot_test_desing.feature.techhealth

import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction
import com.example.daypilot_test_desing.core.data.local.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.data.supabase.dto.InsertTechHealthGroupAppDto
import com.example.daypilot_test_desing.data.supabase.dto.TechHealthConfigDto
import com.example.daypilot_test_desing.data.supabase.dto.TechHealthGroupAppDto
import com.example.daypilot_test_desing.data.supabase.dto.TechHealthGroupConfigDto
import com.example.daypilot_test_desing.data.supabase.dto.UpsertTechHealthGroupDto
import com.example.daypilot_test_desing.data.supabase.supabase
import com.example.daypilot_test_desing.core.reminders.AppUsageTracker
import com.example.daypilot_test_desing.core.reminders.DayPilotAccessibilityService
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TechHealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SharedPrefsTechHealthRepository(application)

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

    fun clearLocalData() {
        repository.clearAll()
        _uiState.value = buildState()
    }

    private suspend fun refreshUsageInternal() {
        val ctx = getApplication<Application>()
        repository.applyPendingChangesIfNewDay()
        val hasPermission = AppUsageTracker.hasPermission(ctx)
        if (hasPermission) {
            val usageMap = AppUsageTracker.getTodayUsage(ctx)
            repository.getAppRestrictions().forEach { r ->
                val used = usageMap[r.packageName] ?: 0
                if (used != r.usedMinutesToday) repository.updateUsage(r.id, used)
            }
            repository.getGroupRestrictions().forEach { g ->
                val used = g.apps.sumOf { usageMap[it.packageName] ?: 0 }
                if (used != g.usedMinutesToday) repository.updateGroupUsage(g.id, used)
            }
        }
        _uiState.value = TechHealthUiState(
            appRestrictions             = repository.getAppRestrictions(),
            groupRestrictions           = repository.getGroupRestrictions(),
            hasUsagePermission          = hasPermission,
            hasAccessibilityPermission  = DayPilotAccessibilityService.isEnabled(ctx),
            techHealthPointEarned       = computePointEarned(),
            activeRestrictionCount      = effectiveRestrictionCount()
        )
    }

    // Editing merges onto the existing stored restriction rather than the form's
    // fresh defaults, so pendingActive/isViolatedToday/usage bookkeeping and the
    // original packageName survive intact, and a limit increase is deferred to
    // the next day like the on/off toggle already is.
    fun saveApp(restriction: AppRestriction) {
        val existing = repository.getAppRestrictions().find { it.id == restriction.id }
        val merged = if (existing != null) {
            val pendingLimit = if (restriction.dailyLimitMinutes == existing.dailyLimitMinutes) null
                                else restriction.dailyLimitMinutes
            existing.copy(pendingLimitMinutes = pendingLimit)
        } else restriction
        repository.saveApp(merged)
        viewModelScope.launch { upsertAppToSupabase(merged) }
        _uiState.value = buildState()
    }

    fun saveGroup(restriction: GroupRestriction) {
        val existing = repository.getGroupRestrictions().find { it.id == restriction.id }
        val merged = if (existing != null) {
            val pendingLimit = if (restriction.dailyLimitMinutes == existing.dailyLimitMinutes) null
                                else restriction.dailyLimitMinutes
            existing.copy(
                groupName           = restriction.groupName,
                apps                = restriction.apps,
                pendingLimitMinutes = pendingLimit
            )
        } else restriction
        repository.saveGroup(merged)
        _uiState.value = buildState()
        viewModelScope.launch {
            val realId = upsertGroupToSupabase(merged)
            // Swap the client-generated placeholder id for the real one so a
            // rename/toggle/delete right after creation targets the right row.
            if (realId != null && realId != merged.id) {
                repository.replaceGroupId(oldId = merged.id, newId = realId)
                _uiState.value = buildState()
            }
        }
    }

    fun toggleRestriction(id: String, enabled: Boolean) {
        repository.toggleRestriction(id, enabled)
        val updated = repository.getAppRestrictions().find { it.id == id }
        if (updated != null) {
            viewModelScope.launch { updatePendingActiveInSupabase(updated.packageName, updated.pendingActive) }
        }
        _uiState.value = buildState()
    }

    fun toggleGroup(id: String, enabled: Boolean) {
        repository.toggleGroup(id, enabled)
        val updated = repository.getGroupRestrictions().find { it.id == id }
        if (updated != null) {
            viewModelScope.launch { updateGroupPendingActiveInSupabase(updated, updated.pendingActive) }
        }
        _uiState.value = buildState()
    }

    fun deleteRestriction(id: String) {
        val pkg = repository.getAppRestrictions().find { it.id == id }?.packageName
        repository.deleteRestriction(id)
        if (pkg != null) viewModelScope.launch { markPendingDeleteInSupabase(pkg) }
        _uiState.value = buildState()
    }

    fun deleteGroup(id: String) {
        val group = repository.getGroupRestrictions().find { it.id == id }
        repository.deleteGroup(id)
        if (group != null) viewModelScope.launch { markGroupPendingDeleteInSupabase(group) }
        _uiState.value = buildState()
    }

    // A purely numeric id means a client-generated placeholder timestamp (not
    // yet synced to Supabase, see saveGroup/replaceGroupId) — fall back to
    // matching by name in that case.
    private fun looksLikeSyncedId(id: String): Boolean = id.toLongOrNull() == null

    private fun isInstalled(packageName: String): Boolean {
        val pm = getApplication<Application>().packageManager
        return try {
            pm.getApplicationInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private suspend fun loadFromSupabase() {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            val configs = supabase.from("tech_health_config").select {
                filter { eq("user_id", uid) }
            }.decodeList<TechHealthConfigDto>()

            val groupConfigs = supabase.from("tech_health_group_config")
                .select(Columns.raw("*, tech_health_group_apps(app_package, app_name)")) {
                    filter { eq("user_id", uid) }
                }.decodeList<TechHealthGroupConfigDto>()

            // Replace, never merge with existing local data — a previous user's
            // leftover restrictions must never blend into this user's list.
            repository.clearAll()

            configs.forEach { dto ->
                if (isInstalled(dto.appPackage)) {
                    repository.saveApp(AppRestriction(
                        id                = dto.appPackage,
                        appName           = dto.appName,
                        packageName       = dto.appPackage,
                        dailyLimitMinutes = (dto.limitHours * 60).toInt(),
                        isEnabled         = dto.isActive,
                        usedMinutesToday  = 0,
                        pendingActive     = dto.pendingActive,
                        pendingLimitMinutes = dto.pendingLimitHours?.let { (it * 60).toInt() },
                        isViolatedToday   = dto.isViolatedToday,
                        pendingDelete     = dto.pendingDelete
                    ))
                } else {
                    deleteUninstalledAppFromSupabase(dto.appPackage)
                }
            }

            groupConfigs.forEach { dto ->
                val installedApps = dto.apps.filter { isInstalled(it.appPackage) }
                if (installedApps.isEmpty() && dto.apps.isNotEmpty()) {
                    deleteUninstalledGroupFromSupabase(dto.id)
                } else {
                    if (installedApps.size != dto.apps.size) {
                        replaceGroupMembership(dto.id, installedApps)
                    }
                    repository.saveGroup(GroupRestriction(
                        id                = dto.id,
                        groupName         = dto.groupName,
                        apps              = installedApps.map {
                            AppRestriction(
                                id                = it.appPackage,
                                appName           = it.appName,
                                packageName       = it.appPackage,
                                dailyLimitMinutes = 0,
                                isEnabled         = true
                            )
                        },
                        dailyLimitMinutes = (dto.limitHours * 60).toInt(),
                        isEnabled         = dto.isActive,
                        usedMinutesToday  = 0,
                        pendingActive     = dto.pendingActive,
                        pendingLimitMinutes = dto.pendingLimitHours?.let { (it * 60).toInt() },
                        isViolatedToday   = dto.isViolatedToday,
                        pendingDelete     = dto.pendingDelete
                    ))
                }
            }
        } catch (_: Exception) { }
        _uiState.value = buildState()
    }

    private suspend fun deleteUninstalledAppFromSupabase(packageName: String) {
        try {
            val uid = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.from("tech_health_config").delete {
                filter { eq("user_id", uid); eq("app_package", packageName) }
            }
        } catch (_: Exception) { }
    }

    private suspend fun deleteUninstalledGroupFromSupabase(groupId: String) {
        try {
            supabase.from("tech_health_group_config").delete {
                filter { eq("id", groupId) }
            }
        } catch (_: Exception) { }
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
            )) { onConflict = "user_id,app_package" }
            // upsert skips false/null defaults (encodeDefaults=false), so clear/set them explicitly
            supabase.from("tech_health_config").update({
                set("pending_delete", false)
                set("pending_active", null as Boolean?)
                set("pending_limit_hours", restriction.pendingLimitMinutes?.let { it / 60.0 })
            }) {
                filter {
                    eq("user_id", uid)
                    eq("app_package", restriction.packageName)
                }
            }
        } catch (_: Exception) { }
    }

    private suspend fun upsertGroupToSupabase(restriction: GroupRestriction): String? {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return null
        return try {
            val groupId: String
            if (looksLikeSyncedId(restriction.id)) {
                // Update by stable id, not name, so a rename updates in place
                // instead of orphaning the old row under a new one.
                supabase.from("tech_health_group_config").update({
                    set("group_name", restriction.groupName)
                    set("limit_hours", restriction.dailyLimitMinutes / 60.0)
                    set("is_active", restriction.isEnabled)
                    set("pending_delete", false)
                    set("pending_active", null as Boolean?)
                    set("pending_limit_hours", restriction.pendingLimitMinutes?.let { it / 60.0 })
                }) {
                    filter { eq("id", restriction.id); eq("user_id", uid) }
                }
                groupId = restriction.id
            } else {
                supabase.from("tech_health_group_config").insert(UpsertTechHealthGroupDto(
                    userId     = uid,
                    groupName  = restriction.groupName,
                    limitHours = restriction.dailyLimitMinutes / 60.0,
                    isActive   = restriction.isEnabled
                ))
                groupId = supabase.from("tech_health_group_config").select {
                    filter { eq("user_id", uid); eq("group_name", restriction.groupName) }
                    limit(1)
                }.decodeList<TechHealthGroupConfigDto>().firstOrNull()?.id ?: return null
            }

            replaceGroupMembership(
                groupId,
                restriction.apps.map { TechHealthGroupAppDto(appPackage = it.packageName, appName = it.appName) }
            )
            groupId
        } catch (_: Exception) { null }
    }

    private suspend fun replaceGroupMembership(groupId: String, apps: List<TechHealthGroupAppDto>) {
        try {
            supabase.from("tech_health_group_apps").delete {
                filter { eq("group_id", groupId) }
            }
            if (apps.isNotEmpty()) {
                supabase.from("tech_health_group_apps").insert(
                    apps.map { InsertTechHealthGroupAppDto(groupId = groupId, appPackage = it.appPackage, appName = it.appName) }
                )
            }
        } catch (_: Exception) { }
    }

    // Soft delete — fn_close_daily_progress() removes it for real that night.
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

    private suspend fun markGroupPendingDeleteInSupabase(group: GroupRestriction) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_group_config").update({
                set("pending_delete", true)
            }) {
                filter {
                    eq("user_id", uid)
                    if (looksLikeSyncedId(group.id)) eq("id", group.id)
                    else eq("group_name", group.groupName)
                }
            }
        } catch (_: Exception) { }
    }

    // Writes pending_active, not is_active — fn_close_daily_progress flips
    // is_active over at the next day's rollover.
    private suspend fun updatePendingActiveInSupabase(packageName: String, pendingActive: Boolean?) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_config").update({
                set("pending_active", pendingActive)
            }) {
                filter {
                    eq("user_id", uid)
                    eq("app_package", packageName)
                }
            }
        } catch (_: Exception) { }
    }

    private suspend fun updateGroupPendingActiveInSupabase(group: GroupRestriction, pendingActive: Boolean?) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("tech_health_group_config").update({
                set("pending_active", pendingActive)
            }) {
                filter {
                    eq("user_id", uid)
                    if (looksLikeSyncedId(group.id)) eq("id", group.id)
                    else eq("group_name", group.groupName)
                }
            }
        } catch (_: Exception) { }
    }

    // Client-side mirror of fn_close_daily_progress's eligibility check, just for
    // an immediate "are you on track" read in the UI — the real +10 is only ever
    // awarded server-side at midnight.
    private fun effectiveRestrictionCount(): Int {
        val apps   = repository.getAppRestrictions().count { it.isEnabled }
        val groups = repository.getGroupRestrictions().filter { it.isEnabled }.sumOf { it.apps.size }
        return apps + groups
    }

    private fun computePointEarned(): Boolean {
        val anyViolated = repository.getAppRestrictions().any { it.isEnabled && it.isViolatedToday } ||
            repository.getGroupRestrictions().any { it.isEnabled && it.isViolatedToday }
        return effectiveRestrictionCount() >= 3 && !anyViolated
    }

    private fun buildState(): TechHealthUiState {
        return TechHealthUiState(
            appRestrictions            = repository.getAppRestrictions(),
            groupRestrictions          = repository.getGroupRestrictions(),
            hasUsagePermission         = AppUsageTracker.hasPermission(getApplication()),
            hasAccessibilityPermission = DayPilotAccessibilityService.isEnabled(getApplication()),
            techHealthPointEarned      = computePointEarned(),
            activeRestrictionCount     = effectiveRestrictionCount()
        )
    }
}
