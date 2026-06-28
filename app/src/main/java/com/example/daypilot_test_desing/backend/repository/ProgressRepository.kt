package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.DayProgress

interface ProgressRepository {
    fun getProgressData(): List<DayProgress>
    fun getRankingPosition(): Int
    fun getPointsToday(): Int
    fun getPointsFromTasks(): Int
    fun getPointsFromSteps(): Int
    fun getPointsFromHabits(): Int
    fun getPointsFromTimers(): Int
    fun addTaskPoints(amount: Int)
    fun removeTaskPoints(amount: Int)
    fun addStepsPoints(amount: Int)
    fun addTimerPoints(amount: Int)
    fun addTechHealthPoints(amount: Int)
    fun getMonthlyPoints(): Int
    fun isTimerCompletedToday(): Boolean
    fun isTechHealthBonusAwarded(): Boolean
}
