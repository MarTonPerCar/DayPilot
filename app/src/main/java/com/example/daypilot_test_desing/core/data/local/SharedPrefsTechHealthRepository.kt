package com.example.daypilot_test_desing.core.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction
import com.example.daypilot_test_desing.core.data.repository.TechHealthRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedPrefsTechHealthRepository(context: Context) : TechHealthRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("daypilot_tech_health", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private fun loadApps(): MutableList<AppRestriction> {
        val raw = prefs.getString("apps", null) ?: return mutableListOf()
        return try {
            json.decodeFromString(ListSerializer(AppRestriction.serializer()), raw).toMutableList()
        } catch (_: Exception) { mutableListOf() }
    }

    private fun saveApps(list: List<AppRestriction>) {
        prefs.edit()
            .putString("apps", json.encodeToString(ListSerializer(AppRestriction.serializer()), list))
            .apply()
    }

    private fun loadGroups(): MutableList<GroupRestriction> {
        val raw = prefs.getString("groups", null) ?: return mutableListOf()
        return try {
            json.decodeFromString(ListSerializer(GroupRestriction.serializer()), raw).toMutableList()
        } catch (_: Exception) { mutableListOf() }
    }

    private fun saveGroups(list: List<GroupRestriction>) {
        prefs.edit()
            .putString("groups", json.encodeToString(ListSerializer(GroupRestriction.serializer()), list))
            .apply()
    }

    override fun getAppRestrictions(): List<AppRestriction>    = loadApps()
    override fun getGroupRestrictions(): List<GroupRestriction> = loadGroups()

    override fun saveApp(restriction: AppRestriction) {
        val apps = loadApps()
        val idx  = apps.indexOfFirst { it.id == restriction.id }
        if (idx >= 0) apps[idx] = restriction else apps.add(restriction)
        saveApps(apps)
    }

    override fun saveGroup(restriction: GroupRestriction) {
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == restriction.id }
        if (idx >= 0) groups[idx] = restriction else groups.add(restriction)
        saveGroups(groups)
    }

    override fun toggleRestriction(id: String, enabled: Boolean) {
        val apps = loadApps()
        val idx  = apps.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val current = apps[idx]
            apps[idx] = current.copy(pendingActive = if (enabled == current.isEnabled) null else enabled)
            saveApps(apps)
        }
    }

    override fun toggleGroup(id: String, enabled: Boolean) {
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == id }
        if (idx >= 0) {
            val current = groups[idx]
            groups[idx] = current.copy(pendingActive = if (enabled == current.isEnabled) null else enabled)
            saveGroups(groups)
        }
    }

    override fun deleteRestriction(id: String) {
        val apps = loadApps()
        val idx  = apps.indexOfFirst { it.id == id }
        if (idx >= 0) { apps[idx] = apps[idx].copy(pendingDelete = true); saveApps(apps) }
    }

    override fun deleteGroup(id: String) {
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == id }
        if (idx >= 0) { groups[idx] = groups[idx].copy(pendingDelete = true); saveGroups(groups) }
    }

    override fun updateUsage(id: String, usedMinutes: Int) {
        val apps = loadApps()
        val idx  = apps.indexOfFirst { it.id == id }
        if (idx >= 0) { apps[idx] = apps[idx].copy(usedMinutesToday = usedMinutes); saveApps(apps) }
    }

    override fun updateGroupUsage(id: String, usedMinutes: Int) {
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == id }
        if (idx >= 0) { groups[idx] = groups[idx].copy(usedMinutesToday = usedMinutes); saveGroups(groups) }
    }

    override fun markViolated(id: String) {
        val apps = loadApps()
        val idx  = apps.indexOfFirst { it.id == id }
        if (idx >= 0 && !apps[idx].isViolatedToday) { apps[idx] = apps[idx].copy(isViolatedToday = true); saveApps(apps) }
    }

    override fun markGroupViolated(id: String) {
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == id }
        if (idx >= 0 && !groups[idx].isViolatedToday) { groups[idx] = groups[idx].copy(isViolatedToday = true); saveGroups(groups) }
    }

    override fun replaceGroupId(oldId: String, newId: String) {
        if (oldId == newId) return
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == oldId }
        if (idx >= 0) { groups[idx] = groups[idx].copy(id = newId); saveGroups(groups) }
    }

    fun clearAll() {
        prefs.edit().remove("apps").remove("groups").apply()
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    fun applyPendingChangesIfNewDay() {
        val lastReset = prefs.getString("last_reset_date", null)
        val today = today()
        if (lastReset == today) return

        saveApps(loadApps().filterNot { it.pendingDelete }.map {
            it.copy(
                isEnabled           = it.pendingActive ?: it.isEnabled,
                pendingActive       = null,
                dailyLimitMinutes   = it.pendingLimitMinutes ?: it.dailyLimitMinutes,
                pendingLimitMinutes = null,
                isViolatedToday     = false,
                usedMinutesToday    = 0
            )
        })
        saveGroups(loadGroups().filterNot { it.pendingDelete }.map {
            it.copy(
                isEnabled           = it.pendingActive ?: it.isEnabled,
                pendingActive       = null,
                dailyLimitMinutes   = it.pendingLimitMinutes ?: it.dailyLimitMinutes,
                pendingLimitMinutes = null,
                isViolatedToday     = false,
                usedMinutesToday    = 0
            )
        })
        prefs.edit().putString("last_reset_date", today).apply()
    }
}
