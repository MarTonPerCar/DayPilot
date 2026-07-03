package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload for INSERT into `points_log`.
 * The DB trigger fn_sync_points_to_progress propagates the insert to
 * daily_progress (by category and total) and to users.total_points_historical.
 */
@Serializable
data class InsertPointsLogDto(
    @SerialName("user_id") val userId: String,
    val points: Int,
    val source: String,
    @SerialName("day_key") val dayKey: String
)

/**
 * Payload for UPSERT into `habits_daily`.
 * UNIQUE(user_id, date) means repeated calls on the same day update the row.
 * The DB trigger fn_sync_habits_to_progress keeps daily_progress.steps in sync.
 */
@Serializable
data class HabitsDailyUpsertDto(
    @SerialName("user_id") val userId: String,
    val date: String,
    val steps: Int = 0,
    @SerialName("steps_goal") val stepsGoal: Int = 10_000
)

/**
 * Row from `user_daily_log` — the closed-day archive used for weekly history charts.
 */
@Serializable
data class DailyLogDto(
    @SerialName("user_id") val userId: String,
    val date: String,
    val steps: Int,
    @SerialName("steps_goal") val stepsGoal: Int = 0,
    @SerialName("tasks_completed") val tasksCompleted: Int,
    @SerialName("tasks_points") val tasksPoints: Int = 0,
    @SerialName("steps_points") val stepsPoints: Int = 0,
    @SerialName("wellness_points") val wellnessPoints: Int = 0,
    @SerialName("timer_points") val timerPoints: Int = 0,
    @SerialName("tech_health_points") val techHealthPoints: Int = 0,
    @SerialName("total_points") val totalPoints: Int
)

/**
 * Upsert payload for `tech_health_config`.
 * Uses UNIQUE(user_id, app_package) as the conflict target.
 */
@Serializable
data class TechHealthConfigDto(
    @SerialName("user_id")        val userId: String,
    @SerialName("app_package")    val appPackage: String,
    @SerialName("app_name")       val appName: String,
    @SerialName("limit_hours")    val limitHours: Double,
    @SerialName("is_active")      val isActive: Boolean = true,
    @SerialName("pending_delete") val pendingDelete: Boolean = false
)

/**
 * Minimal read DTO for checking tech_health_point_earned in `habits_daily`.
 */
@Serializable
data class HabitsDailyReadTechDto(
    @SerialName("tech_health_point_earned") val techHealthPointEarned: Boolean = false
)

/**
 * Upsert payload to write tech_health_point_earned into `habits_daily`.
 * Uses UNIQUE(user_id, date) — only overwrites the tech_health_point_earned column.
 */
@Serializable
data class HabitsDailyTechDto(
    @SerialName("user_id")                  val userId: String,
    val date: String,
    @SerialName("tech_health_point_earned") val techHealthPointEarned: Boolean
)

/**
 * Row from `daily_progress` — the live in-progress counter for today.
 */
@Serializable
data class DailyProgressDto(
    @SerialName("user_id") val userId: String,
    val date: String,
    val steps: Int = 0,
    @SerialName("tasks_completed") val tasksCompleted: Int = 0,
    @SerialName("tasks_points") val tasksPoints: Int = 0,
    @SerialName("steps_points") val stepsPoints: Int = 0,
    @SerialName("wellness_points") val wellnessPoints: Int = 0,
    @SerialName("timer_points") val timerPoints: Int = 0,
    @SerialName("tech_health_points") val techHealthPoints: Int = 0,
    @SerialName("total_points") val totalPoints: Int = 0
)
