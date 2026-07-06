package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificationDto(
    val id: String = "",
    @SerialName("user_id")    val userId: String = "",
    val type: String = "TASK_REMINDER",
    val title: String = "",
    val body: String = "",
    @SerialName("is_read")    val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String = ""
)

@Serializable
data class InsertNotificationDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val type: String,
    val title: String,
    val body: String
)
