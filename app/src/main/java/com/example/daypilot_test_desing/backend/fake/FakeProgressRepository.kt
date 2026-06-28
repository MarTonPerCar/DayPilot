package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.DayProgress
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.backend.supabase.dto.DailyProgressDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object FakeProgressRepository : ProgressRepository {
    private val todayDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    private fun todayStr() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    private val progressData = List(7) { i ->
        val day = todayDay - (6 - i)
        DayProgress(
            day            = if (day < 1) day + 31 else day,
            points         = listOf(120, 95, 140, 80, 160, 60, 110)[i],
            steps          = listOf(8200, 6100, 10200, 5400, 12300, 4800, 7600)[i],
            tasksCompleted = listOf(5, 4, 6, 3, 7, 2, 4)[i]
        )
    }

    private var pointsFromTasks_        = 60
    private var pointsFromSteps_        = 0
    private var pointsFromHabits_       = 0
    private var pointsFromTimers_       = 0
    private var pointsTotal             = 60
    private val monthlyPointsBase       = 420
    private var timerCompletedToday     = false
    private var techHealthBonusAwarded  = false

    // ── ProgressRepository interface ─────────────────────────────────

    override suspend fun getTodayProgress(): DailyProgressDto = DailyProgressDto(
        userId           = "",
        date             = todayStr(),
        tasksPoints      = pointsFromTasks_,
        stepsPoints      = pointsFromSteps_,
        techHealthPoints = pointsFromHabits_,
        timerPoints      = pointsFromTimers_,
        totalPoints      = pointsTotal
    )

    override suspend fun getHistory(days: Int): List<DailyLogDto> = progressData.takeLast(days).map { d ->
        DailyLogDto(
            userId         = "",
            date           = "2024-01-${d.day.toString().padStart(2, '0')}",
            steps          = d.steps,
            tasksCompleted = d.tasksCompleted,
            totalPoints    = d.points
        )
    }

    override suspend fun logPoints(points: Int, source: String) {
        when (source) {
            "TASKS"       -> { pointsFromTasks_ += points; pointsTotal += points }
            "STEPS"       -> { pointsFromSteps_ += points; pointsTotal += points }
            "TIMER"       -> if (points > 0 && !timerCompletedToday) {
                                pointsFromTimers_ += points; pointsTotal += points; timerCompletedToday = true
                             }
            "TECH_HEALTH" -> { pointsFromHabits_ += points; pointsTotal += points }
        }
    }

    override suspend fun getRankingPosition(): Int = FakeRankingRepository.getCurrentUserPosition()

    // ── Non-interface methods (used by ProfileViewModel until Piece 4) ─

    fun getProgressData()     = progressData
    fun getPointsToday()      = pointsTotal
    fun getMonthlyPoints()    = monthlyPointsBase + pointsTotal
    fun getPointsFromTasks()  = pointsFromTasks_
    fun getPointsFromSteps()  = pointsFromSteps_
    fun getPointsFromHabits() = pointsFromHabits_
    fun getPointsFromTimers() = pointsFromTimers_
    fun getRankingPositionSync() = FakeRankingRepository.getCurrentUserPosition()
    fun isTimerCompletedToday()  = timerCompletedToday
    fun isTechHealthBonusAwarded() = techHealthBonusAwarded
}
