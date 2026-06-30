package com.example.daypilot_test_desing.backend.sharedprefs

import android.content.Context
import android.content.SharedPreferences
import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction
import com.example.daypilot_test_desing.backend.repository.TechHealthRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Persists app and group restrictions in SharedPreferences as JSON.
 * There is no server-side table for tech-health rules; they are device-local.
 * usedMinutesToday is saved per session and refreshed from AppUsageTracker on load.
 */
class SharedPrefsTechHealthRepository(context: Context) : TechHealthRepository {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("daypilot_tech_health", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    // ── Apps ─────────────────────────────────────────────────────────

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

    // ── Groups ───────────────────────────────────────────────────────

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

    // ── TechHealthRepository ─────────────────────────────────────────

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
        if (idx >= 0) { apps[idx] = apps[idx].copy(isEnabled = enabled); saveApps(apps) }
    }

    override fun deleteRestriction(id: String) { saveApps(loadApps().filter { it.id != id }) }

    override fun toggleGroup(id: String, enabled: Boolean) {
        val groups = loadGroups()
        val idx    = groups.indexOfFirst { it.id == id }
        if (idx >= 0) { groups[idx] = groups[idx].copy(isEnabled = enabled); saveGroups(groups) }
    }

    override fun deleteGroup(id: String) { saveGroups(loadGroups().filter { it.id != id }) }

    override fun updateUsage(id: String, usedMinutes: Int) {
        val apps = loadApps()
        val idx  = apps.indexOfFirst { it.id == id }
        if (idx >= 0) { apps[idx] = apps[idx].copy(usedMinutesToday = usedMinutes); saveApps(apps) }
    }

    fun clearAll() {
        prefs.edit().remove("apps").remove("groups").apply()
    }
}
