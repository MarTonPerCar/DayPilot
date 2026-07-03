package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FriendsRankingDto(
    val id: String,
    val name: String = "",
    val username: String,
    @SerialName("photo_url") val photoUrl: String? = null,
    val level: Int = 1,
    @SerialName("current_streak") val currentStreak: Int? = null,
    @SerialName("points_last_30_days") val pointsLast30Days: Int = 0
)
