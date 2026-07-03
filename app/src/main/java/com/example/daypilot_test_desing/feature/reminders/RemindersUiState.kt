package com.example.daypilot_test_desing.feature.reminders

import com.example.daypilot_test_desing.core.data.model.ReminderData

data class RemindersUiState(
    val reminders: List<ReminderData> = emptyList()
)
