package com.example.daypilot_test_desing.backend.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("username") val username: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("region") val region: String = "Europe/Madrid",
    @SerialName("created_at") val createdAt: String = "2025",
    @SerialName("level") val level: Int = 1,
    @SerialName("total_points_historical") val totalPointsHistorical: Int = 0,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("longest_streak") val longestStreak: Int = 0
)

@Serializable
data class UpdateUserDto(
    @SerialName("name") val name: String,
    @SerialName("username") val username: String,
    @SerialName("region") val region: String
)
