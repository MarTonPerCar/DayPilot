package com.example.daypilot_test_desing.core.data.model

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

data class DayProgress(
    // Chart x-axis label — the calendar day this point falls on, not its position in the window.
    val dayOfMonth: Int,
    val points: Int,
    val steps: Int,
    val tasksCompleted: Int,
    val isToday: Boolean = false
)

enum class ProgressFilter(@StringRes val labelRes: Int) {
    POINTS(R.string.filter_points),
    STEPS(R.string.filter_steps),
    TASKS(R.string.filter_tasks)
}
