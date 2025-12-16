package com.example.daypilot.main.mainZone.habits.reminders

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.remindersDataStore by preferencesDataStore("reminders_prefs")

class RemindersLocalStore(private val context: Context) {

    private val KEY = stringSetPreferencesKey("reminders_set")

    // id|trigger|title|repeat|enabled|hour|minute|pre|lastPreSentForTriggerAt
    private fun encode(r: Reminder): String {
        val safeTitle = r.title.replace("|", " ")
        val h = r.hour?.toString() ?: ""
        val m = r.minute?.toString() ?: ""
        return listOf(
            r.id,
            r.triggerAtMillis.toString(),
            safeTitle,
            r.repeat.name,
            r.enabled.toString(),
            h,
            m,
            r.preAlertMin.toString(),
            r.lastPreSentForTriggerAt.toString()
        ).joinToString("|")
    }

    private fun decode(s: String): Reminder? {
        val parts = s.split("|", limit = 9)
        if (parts.size < 9) return null
        val id = parts[0]
        val trigger = parts[1].toLongOrNull() ?: return null
        val title = parts[2]
        val repeat = runCatching { RepeatType.valueOf(parts[3]) }.getOrNull() ?: return null
        val enabled = parts[4].toBooleanStrictOrNull() ?: true
        val hour = parts[5].toIntOrNull()
        val minute = parts[6].toIntOrNull()
        val pre = parts[7].toIntOrNull() ?: 0
        val lastPre = parts[8].toLongOrNull() ?: 0L

        return Reminder(
            id = id,
            title = title,
            repeat = repeat,
            enabled = enabled,
            triggerAtMillis = trigger,
            hour = hour,
            minute = minute,
            preAlertMin = pre,
            lastPreSentForTriggerAt = lastPre
        )
    }

    val flow: Flow<List<Reminder>> = context.remindersDataStore.data.map { prefs ->
        (prefs[KEY] ?: emptySet())
            .mapNotNull { decode(it) }
            .sortedBy { it.triggerAtMillis }
    }

    suspend fun add(reminder: Reminder) {
        context.remindersDataStore.edit { prefs ->
            val cur = prefs[KEY] ?: emptySet()
            prefs[KEY] = cur + encode(reminder)
        }
    }

    suspend fun update(reminder: Reminder) {
        context.remindersDataStore.edit { prefs ->
            val cur = prefs[KEY] ?: emptySet()
            val filtered = cur.filterNot { it.startsWith("${reminder.id}|") }.toSet()
            prefs[KEY] = filtered + encode(reminder)
        }
    }

    suspend fun delete(id: String) {
        context.remindersDataStore.edit { prefs ->
            val cur = prefs[KEY] ?: emptySet()
            prefs[KEY] = cur.filterNot { it.startsWith("$id|") }.toSet()
        }
    }

    suspend fun getById(id: String): Reminder? {
        return flow.first().firstOrNull { it.id == id }
    }
}

private fun String.toBooleanStrictOrNull(): Boolean? =
    when (lowercase()) {
        "true" -> true
        "false" -> false
        else -> null
    }