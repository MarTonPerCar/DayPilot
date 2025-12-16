package com.example.daypilot.firebaseLogic.authLogic

data class UserProfile(
    val name: String = "",
    val email: String = "",
    val username: String = "",
    val usernameLower: String = "",
    val region: String = "",
    val photoUrl: String? = null,

    val totalPoints: Long = 0L,

    val todayPoints: Long = 0L,

    val todaySteps: Long = 0L,
    val pointsSteps: Long = 0L,
    val pointsTasks: Long = 0L,
    val pointsWellness: Long = 0L,

    val createdAt: Long = 0L
)
