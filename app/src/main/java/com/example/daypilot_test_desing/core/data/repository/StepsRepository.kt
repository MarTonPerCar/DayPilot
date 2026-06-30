package com.example.daypilot_test_desing.backend.repository

data class StepsWeeklyStats(
    val totalSteps7Days: Int = 0,
    val bestDaySteps: Int = 0,
    val dailyAverage: Int = 0,
    val goalStreak: Int = 0
)

interface StepsRepository {
    // ── Local / sensor state (synchronous, no DB) ──────────────────
    fun getCurrentSteps(): Int
    fun getGoalSteps(): Int
    fun getPendingGoal(): Int?
    fun getPointsEarned(): Int
    fun canChangeGoal(): Boolean
    fun configureGoal(newGoal: Int)
    fun setSteps(steps: Int)
    fun resetMilestones()

    // ── DB-backed (suspend) ────────────────────────────────────────
    suspend fun syncSteps(steps: Int, goal: Int)
    suspend fun getWeeklyStats(): StepsWeeklyStats
}
