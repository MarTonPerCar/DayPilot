package com.example.daypilot_test_desing.backend.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Row returned by the `calendar_tasks` view.
 *
 * Confirmed column names (queried live):
 *   id, user_id, title, description, category, difficulty,
 *   is_completed, estimated_minutes, reminder_enabled, is_recurring, date
 *
 * `id`              = tasks.id
 * `is_completed`    = tasks.is_completed  (toggleTask updates this on `tasks`)
 * `estimated_minutes` = task duration
 * `date`            = task_days.date  ("YYYY-MM-DD")
 * `category`        = Spanish string: "Estudio", "Trabajo", "Salud", etc.
 * `difficulty`      = UPPERCASE string: "EASY", "MEDIUM", "HARD"
 */
@Serializable
data class CalendarTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    val category: String,
    val difficulty: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("reminder_enabled") val reminderEnabled: Boolean = false,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    val date: String
)

/** Payload for INSERT into `tasks`. */
@Serializable
data class NewTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    val category: String,
    val difficulty: String,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    // No default values: supabase-kt uses encodeDefaults=false so Boolean = false would be omitted
    // and the DB would store NULL instead of false. Always serialize these explicitly.
    @SerialName("reminder_enabled") val reminderEnabled: Boolean,
    @SerialName("is_recurring") val isRecurring: Boolean
)

/**
 * Payload for INSERT into `task_days`.
 * Confirmed columns: id (auto), task_id, user_id, date
 * No is_done/is_completed here — completion is on `tasks`.
 */
@Serializable
data class NewTaskDayDto(
    @SerialName("task_id") val taskId: String,
    @SerialName("user_id") val userId: String,
    val date: String
)
