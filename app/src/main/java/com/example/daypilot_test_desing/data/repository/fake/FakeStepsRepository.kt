package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.repository.StepsRepository

object FakeStepsRepository : StepsRepository {
    private var currentSteps = 6_230
    private var goalSteps    = 10_000

    override fun getCurrentSteps()    = currentSteps
    override fun getGoalSteps()       = goalSteps
    override fun getPointsEarned()    = (currentSteps / 1000) * 10
    override fun getTotalSteps7Days() = 48_750
    override fun getBestDaySteps()    = 12_340
    override fun getDailyAverage()    = 6_964
    override fun getGoalStreak()      = 4

    override fun configureGoal(newGoal: Int) { goalSteps = newGoal }
}
