package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.repository.StepsRepository
import com.example.daypilot_test_desing.backend.repository.StepsWeeklyStats
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FakeStepsRepository : StepsRepository {
    private var currentSteps   = 0
    private var activeGoal     = 10_000
    private var pendingGoal: Int? = null
    private var goalChangeDate = ""

    private var milestone1Awarded = false
    private var milestone2Awarded = false
    private var milestone3Awarded = false

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    private fun applyPendingIfNewDay() {
        if (pendingGoal != null && goalChangeDate.isNotEmpty() && goalChangeDate < today()) {
            activeGoal     = pendingGoal!!
            pendingGoal    = null
            goalChangeDate = ""
        }
    }

    override fun getCurrentSteps(): Int  { applyPendingIfNewDay(); return currentSteps }
    override fun getGoalSteps(): Int     { applyPendingIfNewDay(); return activeGoal   }
    override fun getPendingGoal(): Int?  { applyPendingIfNewDay(); return pendingGoal  }

    override fun getPointsEarned(): Int =
        (if (milestone1Awarded) 10 else 0) +
        (if (milestone2Awarded) 20 else 0) +
        (if (milestone3Awarded) 30 else 0)

    override fun canChangeGoal() = goalChangeDate != today()

    override fun configureGoal(newGoal: Int) {
        pendingGoal    = newGoal
        goalChangeDate = today()
    }

    override fun setSteps(steps: Int) {
        currentSteps = steps
        if (!milestone1Awarded && currentSteps >= activeGoal / 3) milestone1Awarded = true
        if (!milestone2Awarded && currentSteps >= (activeGoal * 2) / 3) milestone2Awarded = true
        if (!milestone3Awarded && currentSteps >= activeGoal) milestone3Awarded = true
    }

    override fun resetMilestones() {
        milestone1Awarded = false
        milestone2Awarded = false
        milestone3Awarded = false
    }

    override suspend fun getWeeklyStats(): StepsWeeklyStats = StepsWeeklyStats(
        totalSteps7Days = 48_750,
        bestDaySteps    = 12_340,
        dailyAverage    = 6_964,
        goalStreak      = 4
    )
}
