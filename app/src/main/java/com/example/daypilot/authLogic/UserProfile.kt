package com.example.daypilot.authLogic

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,

    val username: String = "",
    val usernameLower: String = "",
    val region: String = "",

    val createdAt: Long = System.currentTimeMillis(),
    val totalPoints: Long = 0L,
    val pointsSteps: Long = 0L,
    val pointsWellness: Long = 0L,
    val pointsTasks: Long = 0L,

    val todaySteps: Long = 0L,
    val todayStepsDate: String? = null
)