package com.example.daypilot_test_desing.feature.profile

import com.example.daypilot_test_desing.core.data.model.WeeklySummaryData

data class ProfileUiState(
    val name: String = "",
    val username: String = "",
    val email: String = "",
    val memberSince: String = "",
    val level: Int = 1,
    val totalPoints: Int = 0,
    val pointsToNextLevel: Int = 20,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val rankingPosition: Int = 0,
    val pointsToday: Int = 0,
    val pointsFromTasks: Int = 0,
    val pointsFromSteps: Int = 0,
    val pointsFromHabits: Int = 0,
    val pointsFromTimers: Int = 0,
    val stepsToday: Int = 0,
    val tasksCompletedToday: Int = 0,
    val avatarUrl: String? = null,
    val weeklySummary: WeeklySummaryData = WeeklySummaryData(0, 0, 0, 0),
    val isUploadingAvatar: Boolean = false,
    val avatarUploadError: Boolean = false,
    val isSavingProfile: Boolean = false,
    val profileSaveError: Boolean = false,
    val profileSaveSuccess: Boolean = false
)
