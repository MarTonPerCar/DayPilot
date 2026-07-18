package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InsertPointsLogDto(
    @SerialName("user_id") val userId: String,
    val points: Int,
    val source: String,
    @SerialName("day_key") val dayKey: String
)

// Upserts must target the UNIQUE(user_id, date) constraint (onConflict at the
// call site) or repeated calls the same day insert duplicate rows instead of
// updating one.
@Serializable
data class HabitsDailyUpsertDto(
    @SerialName("user_id") val userId: String,
    val date: String,
    val steps: Int = 0,
    @SerialName("steps_goal") val stepsGoal: Int = 10_000
)

@Serializable
data class HabitsDailyReadTimerDto(
    @SerialName("timer_point_earned") val timerPointEarned: Boolean = false
)

// fn_award_steps_milestones (DB trigger) computes this 0-3 level server-side whenever
// steps/steps_goal change — Android only ever reads it back, never computes it locally.
@Serializable
data class HabitsDailyMilestoneDto(
    @SerialName("steps_milestone_level") val stepsMilestoneLevel: Int = 0
)

@Serializable
data class HabitsDailyTimerDto(
    @SerialName("user_id") val userId: String,
    val date: String,
    @SerialName("timer_point_earned") val timerPointEarned: Boolean
)

// Mirrors users.pending_steps_goal / users.pending_steps_goal_date — the same
// columns the pg_cron rollover job reads to promote a queued goal change. Lets
// any device see a goal change queued from another device instead of only the
// device that set it (SharedPreferences alone can't do that).
@Serializable
data class UserPendingGoalDto(
    @SerialName("pending_steps_goal") val pendingStepsGoal: Int? = null,
    @SerialName("pending_steps_goal_date") val pendingStepsGoalDate: String? = null
)

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

@Serializable
data class TechHealthConfigDto(
    @SerialName("user_id")            val userId: String,
    @SerialName("app_package")        val appPackage: String,
    @SerialName("app_name")           val appName: String,
    @SerialName("limit_hours")        val limitHours: Double,
    @SerialName("is_active")          val isActive: Boolean = true,
    @SerialName("pending_active")     val pendingActive: Boolean? = null,
    @SerialName("pending_limit_hours") val pendingLimitHours: Double? = null,
    @SerialName("is_violated_today")  val isViolatedToday: Boolean = false,
    @SerialName("pending_delete")     val pendingDelete: Boolean = false
)

@Serializable
data class TechHealthGroupConfigDto(
    val id: String,
    @SerialName("user_id")            val userId: String,
    @SerialName("group_name")         val groupName: String,
    @SerialName("limit_hours")        val limitHours: Double,
    @SerialName("is_active")          val isActive: Boolean = true,
    @SerialName("pending_active")     val pendingActive: Boolean? = null,
    @SerialName("pending_limit_hours") val pendingLimitHours: Double? = null,
    @SerialName("is_violated_today")  val isViolatedToday: Boolean = false,
    @SerialName("pending_delete")     val pendingDelete: Boolean = false,
    @SerialName("tech_health_group_apps") val apps: List<TechHealthGroupAppDto> = emptyList()
)

@Serializable
data class TechHealthGroupAppDto(
    @SerialName("app_package") val appPackage: String,
    @SerialName("app_name")    val appName: String
)

@Serializable
data class UpsertTechHealthGroupDto(
    @SerialName("user_id")     val userId: String,
    @SerialName("group_name") val groupName: String,
    @SerialName("limit_hours") val limitHours: Double,
    @SerialName("is_active")   val isActive: Boolean = true
)

@Serializable
data class InsertTechHealthGroupAppDto(
    @SerialName("group_id")    val groupId: String,
    @SerialName("app_package") val appPackage: String,
    @SerialName("app_name")    val appName: String
)

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
