package com.example.daypilot_test_desing.viewmodel.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.DayProgress
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
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

    fun refresh() { viewModelScope.launch { load() } }

    private suspend fun load() {
        try {
            val today   = repo.getTodayProgress()
            val history = repo.getHistory(7)
            val ranking = repo.getRankingPosition()
            val progressData = history.map { log ->
                val day = log.date.substringAfterLast("-").toIntOrNull() ?: 0
                DayProgress(day = day, points = log.totalPoints, steps = log.steps, tasksCompleted = log.tasksCompleted)
            }
            _uiState.value = ProgressUiState(
                progressData        = progressData,
                rankingPosition     = ranking,
                pointsToday         = today.totalPoints,
                pointsFromTasks     = today.tasksPoints,
                pointsFromSteps     = today.stepsPoints,
                pointsFromHabits    = today.techHealthPoints + today.wellnessPoints,
                pointsFromTimers    = today.timerPoints,
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
