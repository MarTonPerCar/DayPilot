package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.model.DayProgress
import com.example.daypilot_test_desing.data.repository.ProgressRepository
import java.util.Calendar

object FakeProgressRepository : ProgressRepository {
    private val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    private val progressData = List(7) { i ->
        val day = today - (6 - i)
        DayProgress(
            day            = if (day < 1) day + 31 else day,
            points         = listOf(120, 95, 140, 80, 160, 60, 110)[i],
            steps          = listOf(8200, 6100, 10200, 5400, 12300, 4800, 7600)[i],
            tasksCompleted = listOf(5, 4, 6, 3, 7, 2, 4)[i]
        )
    }

    // All category totals are mutable and start at 0 (only tasks seeded with mock completions)
    private var pointsFromTasks_        = 60   // t1 + t2 + t3 pre-seeded as done × 20 pts
    private var pointsFromSteps_        = 0
    private var pointsFromHabits_       = 0    // tech-health daily bonus
    private var pointsFromTimers_       = 0
    private var pointsTotal             = 60
    // Base accumulated this month before today; total monthly = base + pointsTotal
    private val monthlyPointsBase       = 420
    private var timerCompletedToday     = false
    private var techHealthBonusAwarded  = false

    override fun getProgressData()     = progressData
    override fun getRankingPosition()  = FakeRankingRepository.getCurrentUserPosition()
    override fun getPointsToday()      = pointsTotal
    override fun getMonthlyPoints()    = monthlyPointsBase + pointsTotal
    override fun getPointsFromTasks()  = pointsFromTasks_
    override fun getPointsFromSteps()  = pointsFromSteps_
    override fun getPointsFromHabits() = pointsFromHabits_
    override fun getPointsFromTimers() = pointsFromTimers_

    override fun addTaskPoints(amount: Int) {
        pointsFromTasks_ += amount
        pointsTotal      += amount
    }
    override fun removeTaskPoints(amount: Int) {
        pointsFromTasks_ = maxOf(0, pointsFromTasks_ - amount)
        pointsTotal      = maxOf(0, pointsTotal      - amount)
    }
    override fun addStepsPoints(amount: Int) {
        pointsFromSteps_ += amount
        pointsTotal      += amount
    }
    override fun addTimerPoints(amount: Int) {
        if (!timerCompletedToday) {
            pointsFromTimers_   += amount
            pointsTotal         += amount
            timerCompletedToday  = true
        }
    }
    override fun addTechHealthPoints(amount: Int) {
        if (!techHealthBonusAwarded) {
            pointsFromHabits_      += amount
            pointsTotal            += amount
            techHealthBonusAwarded  = true
        }
    }
    override fun isTimerCompletedToday()    = timerCompletedToday
    override fun isTechHealthBonusAwarded() = techHealthBonusAwarded
}
