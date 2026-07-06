package com.example.daypilot_test_desing.core.data.model

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

data class DayProgress(
    // Calendar day-of-month (1-31) this point actually falls on — used as the
    // chart's x-axis label, not the point's position within the window.
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
