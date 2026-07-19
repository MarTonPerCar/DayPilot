package com.example.daypilot_test_desing.core.data.repository

data class StepsWeeklyStats(
    val totalSteps7Days: Int = 0,
    val bestDaySteps: Int = 0,
    val dailyAverage: Int = 0,
    val goalStreak: Int = 0
)

interface StepsRepository {
    fun getCurrentSteps(): Int
    fun getGoalSteps(): Int
    fun getPendingGoal(): Int?
    fun canChangeGoal(): Boolean
    fun configureGoal(newGoal: Int)
    fun setSteps(steps: Int)

    // fn_award_steps_milestones computes the level server-side; this only reads it.
    suspend fun getPointsEarned(): Int
    suspend fun syncSteps(steps: Int, goal: Int)
    suspend fun getWeeklyStats(): StepsWeeklyStats
    suspend fun hydrateGoalFromServer()
}
