package com.example.daypilot_test_desing.core.data.repository

import com.example.daypilot_test_desing.core.data.model.ReminderData
import com.example.daypilot_test_desing.core.data.model.ReminderFormDataInfo

interface ReminderRepository {
    fun getReminders(): List<ReminderData>
    fun addReminder(form: ReminderFormDataInfo): ReminderData
    fun deleteReminder(id: String)
    fun toggleReminder(id: String, enabled: Boolean)
}
