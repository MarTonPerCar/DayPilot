package com.example.daypilot_test_desing.viewmodel.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.DayProgress
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import java.util.Calendar
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressViewModel(
    application: Application,
    private val repo: ProgressRepository
) : AndroidViewModel(application) {

    private val appPrefs = AppPreferences(application)

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    private suspend fun load() {
        try {
            val todayProgress = repo.getTodayProgress()
            // Closed days descending → reverse to chronological, then append today
            val history = repo.getHistory(30)
            val ranking = repo.getRankingPosition()
            val closedData = history.reversed().map { log ->
                val day = log.date.substringAfterLast("-").toIntOrNull() ?: 0
                DayProgress(day = day, points = log.totalPoints, steps = log.steps, tasksCompleted = log.tasksCompleted)
            }
            val todayDayNum = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val progressData = closedData + DayProgress(
                day            = todayDayNum,
                points         = todayProgress.totalPoints,
                steps          = todayProgress.steps,
                tasksCompleted = todayProgress.tasksCompleted,
                isToday        = true
            )
            _uiState.value = ProgressUiState(
                progressData        = progressData,
                rankingPosition     = ranking,
                pointsToday         = todayProgress.totalPoints,
                pointsFromTasks     = todayProgress.tasksPoints,
                pointsFromSteps     = todayProgress.stepsPoints,
                pointsFromHabits    = todayProgress.techHealthPoints + todayProgress.wellnessPoints,
                pointsFromTimers    = todayProgress.timerPoints,
                timerCompletedToday = appPrefs.timerPointsDate == today()
            )
        } catch (_: Exception) { }
    }

    fun recordTimerComplete() {
        val todayStr = today()
        if (appPrefs.timerPointsDate == todayStr) return
        appPrefs.timerPointsDate = todayStr
        viewModelScope.launch {
            repo.logPoints(10, "TIMER")
            load()
        }
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    companion object {
        fun factory(application: Application, repo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProgressViewModel(application, repo) as T
            }
    }
}
