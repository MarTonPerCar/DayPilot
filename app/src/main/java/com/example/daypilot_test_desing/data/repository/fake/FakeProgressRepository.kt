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

    override fun getProgressData()      = progressData
    override fun getRankingPosition()   = 3
    override fun getPointsToday()       = 110
    override fun getPointsFromTasks()   = 60
    override fun getPointsFromSteps()   = 20
    override fun getPointsFromHabits()  = 20
    override fun getPointsFromTimers()  = 10
}
