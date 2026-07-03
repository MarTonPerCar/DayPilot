package com.example.daypilot_test_desing.core.data.model

data class FriendWeeklySummary(
    val totalPoints: Int,
    val tasksCompleted: Int,
    val totalSteps: Int,
    val bestStreak: Int,
    val myReaction: ReactionType? = null
)

data class FriendData(
    val id: String,
    val name: String,
    val email: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null,
    val weeklySummary: FriendWeeklySummary? = null
)