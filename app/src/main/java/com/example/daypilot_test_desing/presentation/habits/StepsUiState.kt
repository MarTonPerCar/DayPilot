package com.example.daypilot_test_desing.presentation.habits

data class StepsUiState(
    val currentSteps: Int = 0,
    val goalSteps: Int = 10_000,
    val pointsEarned: Int = 0,
    val pointsRemaining: Int = 0,
    val totalSteps7Days: Int = 0,
    val bestDaySteps: Int = 0,
    val dailyAverage: Int = 0,
    val goalStreak: Int = 0
)
