package com.example.daypilot_test_desing.presentation.home

import com.example.daypilot_test_desing.data.model.DayProgress

data class HomeUiState(
    val userName: String = "",
    val streak: Int = 0,
    val stepsToday: Int = 0,
    val stepsGoal: Int = 10_000,
    val tasksCompleted: Int = 0,
    val tasksTotal: Int = 0,
    val progressData: List<DayProgress> = emptyList(),
    val pointsToday: Int = 0,
    val rankingPosition: Int = 0
)
