package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// occurrenceId (task_days.id) is one date; taskId (tasks.id) is the whole recurring
// series — completing uses occurrenceId, editing/deleting uses taskId.
// category is a Spanish label string, difficulty is uppercase ("EASY"/"MEDIUM"/"HARD").
@Serializable
data class CalendarTaskDto(
    @SerialName("occurrence_id") val occurrenceId: String,
    @SerialName("task_id") val taskId: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    val category: String,
    val difficulty: String,
    @SerialName("is_completed") val isCompleted: Boolean,
    @SerialName("is_earned") val isEarned: Boolean = false,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    @SerialName("reminder_enabled") val reminderEnabled: Boolean = false,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    val date: String
)

@Serializable
data class NewTaskDto(
    val id: String,
    @SerialName("user_id") val userId: String,
    val title: String,
    val description: String? = null,
    val category: String,
    val difficulty: String,
    @SerialName("estimated_minutes") val estimatedMinutes: Int,
    // no defaults here, encodeDefaults=false would drop a false and store NULL instead
    @SerialName("reminder_enabled") val reminderEnabled: Boolean,
    @SerialName("is_recurring") val isRecurring: Boolean
)

@Serializable
data class NewTaskDayDto(
    @SerialName("task_id") val taskId: String,
    @SerialName("user_id") val userId: String,
    val date: String
)

@Serializable
data class TaskIdDto(val id: String)
