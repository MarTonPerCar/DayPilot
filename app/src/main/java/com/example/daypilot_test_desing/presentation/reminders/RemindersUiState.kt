package com.example.daypilot_test_desing.presentation.reminders

import com.example.daypilot_test_desing.data.model.ReminderData

data class RemindersUiState(
    val reminders: List<ReminderData> = emptyList()
)
