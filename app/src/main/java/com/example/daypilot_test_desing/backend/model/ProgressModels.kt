package com.example.daypilot_test_desing.backend.model

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

data class DayProgress(
    val day: Int,
    val points: Int,
    val steps: Int,
    val tasksCompleted: Int
)

enum class ProgressFilter(@StringRes val labelRes: Int) {
    POINTS(R.string.filter_points),
    STEPS(R.string.filter_steps),
    TASKS(R.string.filter_tasks)
}
