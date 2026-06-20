package com.example.daypilot_test_desing.viewmodel.calendar

import com.example.daypilot_test_desing.data.model.CalendarTaskData

data class CalendarUiState(
    val tasks: List<CalendarTaskData> = emptyList()
)
