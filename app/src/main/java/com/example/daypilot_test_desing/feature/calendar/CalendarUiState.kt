package com.example.daypilot_test_desing.feature.calendar

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData

data class CalendarUiState(
    val tasks: List<CalendarTaskData> = emptyList(),
    val isLoading: Boolean = false,
    @StringRes val userMessage: Int? = null
)
