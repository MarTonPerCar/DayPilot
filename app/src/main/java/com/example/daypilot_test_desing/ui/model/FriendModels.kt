package com.example.daypilot_test_desing.ui.model

import com.example.daypilot_test_desing.ui.components.basic.ReactionType

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