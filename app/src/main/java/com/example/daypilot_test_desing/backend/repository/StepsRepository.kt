package com.example.daypilot_test_desing.backend.repository

interface StepsRepository {
    fun getCurrentSteps(): Int
    fun getGoalSteps(): Int
    fun getPendingGoal(): Int?
    fun getPointsEarned(): Int
    fun getTotalSteps7Days(): Int
    fun getBestDaySteps(): Int
    fun getDailyAverage(): Int
    fun getGoalStreak(): Int
    fun configureGoal(newGoal: Int)
    fun canChangeGoal(): Boolean
    fun setSteps(steps: Int)
}
