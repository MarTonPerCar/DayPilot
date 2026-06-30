package com.example.daypilot_test_desing.viewmodel.calendar

import com.example.daypilot_test_desing.backend.model.CalendarTaskData

data class CalendarUiState(
    val tasks: List<CalendarTaskData> = emptyList(),
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
)
