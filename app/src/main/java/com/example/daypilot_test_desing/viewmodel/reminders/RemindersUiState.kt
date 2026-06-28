package com.example.daypilot_test_desing.viewmodel.reminders

import com.example.daypilot_test_desing.backend.model.ReminderData

data class RemindersUiState(
    val reminders: List<ReminderData> = emptyList()
)
