package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.FrequencyType
import com.example.daypilot_test_desing.backend.model.ReminderData
import com.example.daypilot_test_desing.backend.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.backend.repository.ReminderRepository
import java.util.UUID

object FakeReminderRepository : ReminderRepository {
    private val reminders = mutableListOf<ReminderData>()

    override fun getReminders(): List<ReminderData> = reminders.toList()

    override fun addReminder(form: ReminderFormDataInfo): ReminderData {
        val time = when {
            form.quickMinutes != null -> "${form.quickMinutes} min"
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
            isEnabled       = true
        )
        reminders.add(reminder)
        return reminder
    }

    override fun deleteReminder(id: String) { reminders.removeAll { it.id == id } }

    override fun toggleReminder(id: String, enabled: Boolean) {
        val idx = reminders.indexOfFirst { it.id == id }
        if (idx >= 0) reminders[idx] = reminders[idx].copy(isEnabled = enabled)
    }
}
