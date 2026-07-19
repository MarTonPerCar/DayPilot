package com.example.daypilot_test_desing.core.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.daypilot_test_desing.core.data.model.ReminderData
import com.example.daypilot_test_desing.core.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.core.data.repository.ReminderRepository
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.UUID

// Reminders are device-local only — no server-side table, unlike the rest of the app's data.
class SharedPrefsReminderRepository(context: Context) : ReminderRepository {

    companion object {
        private const val TAG = "SharedPrefsReminderRepo"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("daypilot_reminders", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private fun load(): MutableList<ReminderData> {
        val raw = prefs.getString("reminders", null) ?: return mutableListOf()
        return try {
            json.decodeFromString(ListSerializer(ReminderData.serializer()), raw).toMutableList()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode stored reminders, discarding local cache", e)
            mutableListOf()
        }
    }

    private fun save(list: List<ReminderData>) {
        prefs.edit()
            .putString("reminders", json.encodeToString(ListSerializer(ReminderData.serializer()), list))
            .apply()
    }

    override fun getReminders(): List<ReminderData> = load()

    override fun addReminder(form: ReminderFormDataInfo): ReminderData {
        val time = when {
            form.quickMinutes != null      -> "${form.quickMinutes} min"
            form.scheduledDateTime != null -> {
                val h = form.scheduledDateTime.get(java.util.Calendar.HOUR_OF_DAY)
                val m = form.scheduledDateTime.get(java.util.Calendar.MINUTE)
                "%02d:%02d".format(h, m)
            }
            else -> "00:00"
        }
        val reminder = ReminderData(
            id              = UUID.randomUUID().toString(),
            title           = form.title,
            time            = time,
            triggerAtMillis = form.triggerAtMillis,
            isEnabled       = true,
            frequencyType   = form.frequencyType
        )
        val list = load()
        list.add(reminder)
        save(list)
        return reminder
    }

    override fun deleteReminder(id: String) { save(load().filter { it.id != id }) }

    override fun toggleReminder(id: String, enabled: Boolean) {
        val list = load()
        val idx  = list.indexOfFirst { it.id == id }
        if (idx >= 0) { list[idx] = list[idx].copy(isEnabled = enabled); save(list) }
    }

    fun updateTriggerTime(id: String, millis: Long) {
        val list = load()
        val idx  = list.indexOfFirst { it.id == id }
        if (idx >= 0) { list[idx] = list[idx].copy(triggerAtMillis = millis); save(list) }
    }
}
