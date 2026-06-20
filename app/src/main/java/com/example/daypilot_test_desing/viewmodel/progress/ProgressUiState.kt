package com.example.daypilot_test_desing.viewmodel.progress

import com.example.daypilot_test_desing.data.model.DayProgress

data class ProgressUiState(
    val progressData: List<DayProgress> = emptyList(),
    val rankingPosition: Int = 0,
    val pointsToday: Int = 0,
    val pointsFromTasks: Int = 0,
    val pointsFromSteps: Int = 0,
    val pointsFromHabits: Int = 0,
    val pointsFromTimers: Int = 0,
    val timerCompletedToday: Boolean = false
)
