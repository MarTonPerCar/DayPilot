package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.repository.StepsRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FakeStepsRepository : StepsRepository {
    private var currentSteps   = 0
    private var activeGoal     = 10_000
    private var pendingGoal: Int? = null
    private var goalChangeDate = ""

    // Milestone tracking — reset on new day (handled by baseline in StepsViewModel)
    var milestone1Awarded = false   // first third of daily goal  → +10 pts
    var milestone2Awarded = false   // two thirds of daily goal   → +20 pts
    var milestone3Awarded = false   // full daily goal            → +30 pts

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

    // Total step points awarded so far today (milestones: 10 + 20 + 30 = 60 max)
    override fun getPointsEarned(): Int =
        (if (milestone1Awarded) 10 else 0) +
        (if (milestone2Awarded) 20 else 0) +
        (if (milestone3Awarded) 30 else 0)

    override fun getTotalSteps7Days() = 48_750
    override fun getBestDaySteps()    = 12_340
    override fun getDailyAverage()    = 6_964
    override fun getGoalStreak()      = 4

    override fun canChangeGoal() = goalChangeDate != today()

    override fun configureGoal(newGoal: Int) {
        pendingGoal    = newGoal
        goalChangeDate = today()
    }

    // Called by StepsViewModel sensor listener with the computed daily step count
    override fun setSteps(steps: Int) {
        currentSteps = steps
        awardMilestones()
    }

    private fun awardMilestones() {
        val goal = activeGoal
        if (!milestone1Awarded && currentSteps >= goal / 3) {
            milestone1Awarded = true
            FakeProgressRepository.addStepsPoints(10)
        }
        if (!milestone2Awarded && currentSteps >= (goal * 2) / 3) {
            milestone2Awarded = true
            FakeProgressRepository.addStepsPoints(20)
        }
        if (!milestone3Awarded && currentSteps >= goal) {
            milestone3Awarded = true
            FakeProgressRepository.addStepsPoints(30)
        }
    }
}
