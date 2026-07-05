package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    @SerialName("name") val name: String = "",
    @SerialName("username") val username: String = "",
    @SerialName("email") val email: String = "",
    @SerialName("photo_url") val photoUrl: String? = null,
    @SerialName("region") val region: String? = null,
    @SerialName("created_at") val createdAt: String = "2025",
    @SerialName("level") val level: Int = 1,
    @SerialName("total_points_historical") val totalPointsHistorical: Int = 0,
    @SerialName("points_to_next_level") val pointsToNextLevel: Int = 20
)

@Serializable
data class UserStreakDto(
    @SerialName("user_id") val userId: String,
    @SerialName("current_streak") val currentStreak: Int = 0,
    @SerialName("longest_streak") val longestStreak: Int = 0
)

@Serializable
data class UpdateUserDto(
    @SerialName("name") val name: String,
    @SerialName("username") val username: String,
    @SerialName("username_lower") val usernameLower: String,
    @SerialName("region") val region: String
)

@Serializable
data class FriendRowDto(
    @SerialName("requester_id") val requesterId: String,
    @SerialName("receiver_id") val receiverId: String
)

@Serializable
data class FriendRequestDto(
    val id: String,
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("to_user_id") val toUserId: String
)

@Serializable
data class InsertFriendRequestDto(
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("to_user_id") val toUserId: String
)

@Serializable
data class InsertFriendDto(
    @SerialName("requester_id") val requesterId: String,
    @SerialName("receiver_id") val receiverId: String
)

@Serializable
data class WeeklySummaryRowDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("week_start") val weekStart: String,
    @SerialName("total_steps") val totalSteps: Int = 0,
    @SerialName("total_tasks_completed") val totalTasksCompleted: Int = 0,
    @SerialName("total_points") val totalPoints: Int = 0,
    @SerialName("best_streak") val bestStreak: Int = 0
)

@Serializable
data class InsertReactionDto(
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("to_user_id") val toUserId: String,
    @SerialName("weekly_summary_id") val weeklySummaryId: String,
    val type: String
)

@Serializable
data class ReactionDto(
    @SerialName("from_user_id") val fromUserId: String,
    @SerialName("to_user_id") val toUserId: String,
    @SerialName("weekly_summary_id") val weeklySummaryId: String,
    val type: String
)

@Serializable
data class SentRequestDto(
    @SerialName("to_user_id") val toUserId: String
)
