package com.example.daypilot_test_desing.viewmodel.habits

data class HabitsUiState(
    val currentSteps: Int = 0,
    val goalSteps: Int = 10_000,
    val pointsEarned: Int = 0,
    val pointsRemaining: Int = 0,
    val goalChangedToday: Boolean = false,
    val pendingGoal: Int? = null
)
