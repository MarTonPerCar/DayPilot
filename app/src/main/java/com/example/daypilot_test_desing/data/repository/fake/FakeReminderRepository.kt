package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.model.FrequencyType
import com.example.daypilot_test_desing.data.model.ReminderData
import com.example.daypilot_test_desing.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.data.repository.ReminderRepository
import java.util.UUID

object FakeReminderRepository : ReminderRepository {
    private val reminders = mutableListOf(
        ReminderData("r1", "Tomar agua",            "09:00", isEnabled = true),
        ReminderData("r2", "Sesión de ejercicio",   "18:30", isEnabled = true),
        ReminderData("r3", "Revisar tareas del día","07:30", isEnabled = false),
        ReminderData("r4", "Meditación nocturna",   "22:00", isEnabled = true)
    )

    override fun getReminders(): List<ReminderData> = reminders.toList()

    override fun addReminder(form: ReminderFormDataInfo) {
        val time = when {
            form.quickMinutes != null -> "${form.quickMinutes} min"
            form.scheduledDateTime != null -> {
                val h = form.scheduledDateTime.get(java.util.Calendar.HOUR_OF_DAY)
                val m = form.scheduledDateTime.get(java.util.Calendar.MINUTE)
                "%02d:%02d".format(h, m)
            }
            else -> "00:00"
        }
        reminders.add(
            ReminderData(
                id        = UUID.randomUUID().toString(),
                title     = form.title,
                time      = time,
                isEnabled = true
            )
        )
    }

    override fun deleteReminder(id: String) { reminders.removeAll { it.id == id } }

    override fun toggleReminder(id: String, enabled: Boolean) {
        val idx = reminders.indexOfFirst { it.id == id }
        if (idx >= 0) reminders[idx] = reminders[idx].copy(isEnabled = enabled)
    }
}
