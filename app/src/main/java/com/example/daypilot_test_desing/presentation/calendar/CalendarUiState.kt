package com.example.daypilot_test_desing.presentation.calendar

import com.example.daypilot_test_desing.data.model.CalendarTaskData

data class CalendarUiState(
    val tasks: List<CalendarTaskData> = emptyList()
)
