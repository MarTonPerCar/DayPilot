package com.example.daypilot.main.mainZone.habits.tech

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.techStore by preferencesDataStore("tech_health_v2") // si quieres reset automático: cambia a tech_health_v3

class TechHealthLocalStore(private val context: Context) {

    private object Keys {
        val GROUP_IDS = stringSetPreferencesKey("group_ids")
        val RESTRICTIONS = stringSetPreferencesKey("restrictions")

        // ✅ Uso “real” (tu contador)
        val USAGE_DAY = stringPreferencesKey("usage_day_key")
        val USAGE_SET = stringSetPreferencesKey("usage_ms_set")
    }

    private fun kGroupName(id: String) = stringPreferencesKey("group_name_$id")
    private fun kGroupApps(id: String) = stringSetPreferencesKey("group_apps_$id")

    // ---------- Restriction encode/decode ----------
    private fun enc(r: Restriction): String {
        fun safe(s: String) = s.replace("|", " ")
        return listOf(
            r.id,
            r.type.name,
            r.targetId,
            safe(r.displayName),

            r.activeEnabled.toString(),
            r.activeLimitMin.toString(),

            r.activeNotifyEnabled.toString(),
            r.activeNotifyEverySec.toString(),

            (r.pendingEnabled?.toString() ?: ""),
            (r.pendingLimitMin?.toString() ?: ""),
            (r.pendingNotifyEnabled?.toString() ?: ""),
            (r.pendingNotifyEverySec?.toString() ?: ""),
            (r.pendingSinceDayKey ?: ""),

            r.createdAt.toString(),
            r.updatedAt.toString()
        ).joinToString("|")
    }

    private fun dec(s: String): Restriction? {
        val p = s.split("|", limit = 15)
        if (p.size < 11) return null // tolera viejos, pero mínimos

        val id = p[0]
        val type = runCatching { RestrictionType.valueOf(p[1]) }.getOrNull() ?: return null
        val targetId = p[2]
        val display = p[3]

        val activeEnabled = p[4].toBooleanStrictOrNull() ?: true
        val activeLimit = p[5].toIntOrNull() ?: return null

        // Defaults si viene de data vieja
        val activeNotifyEnabled = p.getOrNull(6)?.toBooleanStrictOrNull() ?: true
        val activeNotifyEverySec = p.getOrNull(7)?.toIntOrNull()?.coerceIn(5, 60) ?: 60

        val pendingEnabled = p.getOrNull(8)?.takeIf { it.isNotBlank() }?.toBooleanStrictOrNull()
        val pendingLimit = p.getOrNull(9)?.takeIf { it.isNotBlank() }?.toIntOrNull()
        val pendingNotifyEnabled = p.getOrNull(10)?.takeIf { it.isNotBlank() }?.toBooleanStrictOrNull()
        val pendingNotifyEverySec = p.getOrNull(11)?.takeIf { it.isNotBlank() }?.toIntOrNull()?.coerceIn(5, 60)
        val pendingDay = p.getOrNull(12)?.takeIf { it.isNotBlank() }

        val createdAt = p.getOrNull(13)?.toLongOrNull() ?: System.currentTimeMillis()
        val updatedAt = p.getOrNull(14)?.toLongOrNull() ?: System.currentTimeMillis()

        return Restriction(
            id = id,
            type = type,
            targetId = targetId,
            displayName = display,

            activeEnabled = activeEnabled,
            activeLimitMin = activeLimit,

            activeNotifyEnabled = activeNotifyEnabled,
            activeNotifyEverySec = activeNotifyEverySec,

            pendingEnabled = pendingEnabled,
            pendingLimitMin = pendingLimit,
            pendingNotifyEnabled = pendingNotifyEnabled,
            pendingNotifyEverySec = pendingNotifyEverySec,
            pendingSinceDayKey = pendingDay,

            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    val flow: Flow<Pair<List<GroupDef>, List<Restriction>>> =
        context.techStore.data.map { prefs ->
            val groupIds = (prefs[Keys.GROUP_IDS] ?: emptySet()).toList().sorted()
            val groups = groupIds.map { id ->
                GroupDef(
                    id = id,
                    name = prefs[kGroupName(id)] ?: "Grupo",
                    appPkgs = prefs[kGroupApps(id)] ?: emptySet()
                )
            }

            val restrictions = (prefs[Keys.RESTRICTIONS] ?: emptySet())
                .mapNotNull { dec(it) }
                .sortedBy { it.createdAt }

            groups to restrictions
        }

    // ---------- Groups ----------
    suspend fun createGroup(id: String, name: String) {
        context.techStore.edit { p ->
            val cur = p[Keys.GROUP_IDS] ?: emptySet()
            p[Keys.GROUP_IDS] = cur + id
            p[kGroupName(id)] = name.trim().ifBlank { "Grupo" }
            p[kGroupApps(id)] = emptySet()
        }
    }

    suspend fun setGroupApps(id: String, pkgs: Set<String>) {
        context.techStore.edit { p ->
            p[kGroupApps(id)] = pkgs
        }
    }

    suspend fun renameGroup(id: String, name: String) {
        context.techStore.edit { p ->
            p[kGroupName(id)] = name.trim().ifBlank { "Grupo" }
        }
    }

    suspend fun deleteGroup(id: String) {
        context.techStore.edit { p ->
            val cur = p[Keys.GROUP_IDS] ?: emptySet()
            p[Keys.GROUP_IDS] = cur - id
            p.remove(kGroupName(id))
            p.remove(kGroupApps(id))
        }
    }

    // ---------- Restrictions ----------
    suspend fun addRestriction(r: Restriction) {
        context.techStore.edit { p ->
            val cur = p[Keys.RESTRICTIONS] ?: emptySet()
            p[Keys.RESTRICTIONS] = cur + enc(r)
        }
    }

    suspend fun updateRestriction(r: Restriction) {
        context.techStore.edit { p ->
            val cur = p[Keys.RESTRICTIONS] ?: emptySet()
            val filtered = cur.filterNot { it.startsWith("${r.id}|") }.toSet()
            p[Keys.RESTRICTIONS] = filtered + enc(r.copy(updatedAt = System.currentTimeMillis()))
        }
    }

    suspend fun deleteRestrictionHard(id: String) {
        context.techStore.edit { p ->
            val cur = p[Keys.RESTRICTIONS] ?: emptySet()
            p[Keys.RESTRICTIONS] = cur.filterNot { it.startsWith("$id|") }.toSet()
        }
    }

    suspend fun rolloverIfNewDay(todayKey: String) {
        context.techStore.edit { p ->
            val cur = p[Keys.RESTRICTIONS] ?: emptySet()
            val decoded = cur.mapNotNull { dec(it) }

            val updated = decoded.map { r ->
                val pendDay = r.pendingSinceDayKey
                if (pendDay != null && pendDay != todayKey) {
                    val newEnabled = r.pendingEnabled ?: r.activeEnabled
                    val newLimit = r.pendingLimitMin ?: r.activeLimitMin

                    val newNotifEnabled = r.pendingNotifyEnabled ?: r.activeNotifyEnabled
                    val newEverySec = r.pendingNotifyEverySec ?: r.activeNotifyEverySec

                    r.copy(
                        activeEnabled = newEnabled,
                        activeLimitMin = newLimit,
                        activeNotifyEnabled = newNotifEnabled,
                        activeNotifyEverySec = newEverySec,

                        pendingEnabled = null,
                        pendingLimitMin = null,
                        pendingNotifyEnabled = null,
                        pendingNotifyEverySec = null,
                        pendingSinceDayKey = null
                    )
                } else r
            }

            p[Keys.RESTRICTIONS] = updated.map { enc(it) }.toSet()
        }
    }

    // ---------- Usage (tu contador) ----------
    val usageTodayFlow: Flow<Map<String, Long>> =
        context.techStore.data.map { prefs ->
            val set = prefs[Keys.USAGE_SET] ?: emptySet()
            set.mapNotNull { item ->
                val parts = item.split("|", limit = 2)
                if (parts.size != 2) null
                else parts[0] to (parts[1].toLongOrNull() ?: 0L)
            }.toMap()
        }

    suspend fun addUsageDelta(todayKey: String, pkg: String, deltaMs: Long) {
        if (deltaMs <= 0) return
        context.techStore.edit { p ->
            val curDay = p[Keys.USAGE_DAY]
            if (curDay != todayKey) {
                p[Keys.USAGE_DAY] = todayKey
                p[Keys.USAGE_SET] = emptySet()
            }

            val curSet = p[Keys.USAGE_SET] ?: emptySet()
            val map = curSet.mapNotNull { item ->
                val parts = item.split("|", limit = 2)
                if (parts.size != 2) null else parts[0] to (parts[1].toLongOrNull() ?: 0L)
            }.toMap().toMutableMap()

            map[pkg] = (map[pkg] ?: 0L) + deltaMs
            p[Keys.USAGE_SET] = map.entries.map { "${it.key}|${it.value}" }.toSet()
        }
    }

    suspend fun resetUsageForDay(todayKey: String) {
        context.techStore.edit { p ->
            p[Keys.USAGE_DAY] = todayKey
            p[Keys.USAGE_SET] = emptySet()
        }
    }

}

private fun String.toBooleanStrictOrNull(): Boolean? =
    when (lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }