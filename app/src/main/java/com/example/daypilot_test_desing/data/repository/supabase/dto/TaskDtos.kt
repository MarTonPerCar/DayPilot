package com.example.daypilot_test_desing.data.repository.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Row returned by the `calendar_tasks` view.
 * id          → tasks.id
 * scheduled_date → task_days.scheduled_date (ISO-8601 "YYYY-MM-DD")
 * is_done     → task_days.is_done
 */
@Serializable
data class CalendarTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val category: String,
    val difficulty: String,
    @SerialName("duration_minutes") val durationMinutes: Int,
    @SerialName("scheduled_date") val scheduledDate: String,
    @SerialName("is_done") val isDone: Boolean
)

/** Payload for INSERT into `tasks`. */
@Serializable
data class NewTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val category: String,
    val difficulty: String,
    @SerialName("duration_minutes") val durationMinutes: Int
)

/** Payload for INSERT into `task_days`. */
@Serializable
data class NewTaskDayDto(
    @SerialName("task_id") val taskId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("scheduled_date") val scheduledDate: String,
    @SerialName("is_done") val isDone: Boolean = false
)
