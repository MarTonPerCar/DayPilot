package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.DayProgress

interface ProgressRepository {
    fun getProgressData(): List<DayProgress>
    fun getRankingPosition(): Int
    fun getPointsToday(): Int
    fun getPointsFromTasks(): Int
    fun getPointsFromSteps(): Int
    fun getPointsFromHabits(): Int
    fun getPointsFromTimers(): Int
}
