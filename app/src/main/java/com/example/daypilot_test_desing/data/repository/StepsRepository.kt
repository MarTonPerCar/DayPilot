package com.example.daypilot_test_desing.data.repository

interface StepsRepository {
    fun getCurrentSteps(): Int
    fun getGoalSteps(): Int
    fun getPointsEarned(): Int
    fun getTotalSteps7Days(): Int
    fun getBestDaySteps(): Int
    fun getDailyAverage(): Int
    fun getGoalStreak(): Int
    fun configureGoal(newGoal: Int)
}
