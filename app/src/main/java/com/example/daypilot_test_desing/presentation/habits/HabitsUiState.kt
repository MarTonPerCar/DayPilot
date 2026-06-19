package com.example.daypilot_test_desing.presentation.habits

data class HabitsUiState(
    val currentSteps: Int = 0,
    val goalSteps: Int = 10_000,
    val pointsEarned: Int = 0,
    val pointsRemaining: Int = 0
)
