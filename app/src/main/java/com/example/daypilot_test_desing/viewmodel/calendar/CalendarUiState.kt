package com.example.daypilot_test_desing.viewmodel.calendar

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.backend.model.CalendarTaskData

data class CalendarUiState(
    val tasks: List<CalendarTaskData> = emptyList(),
    val isLoading: Boolean = false,
    @StringRes val userMessage: Int? = null
)
