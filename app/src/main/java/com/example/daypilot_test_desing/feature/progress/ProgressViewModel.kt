package com.example.daypilot_test_desing.feature.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.buildProgressWindow
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
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

    fun invalidate() { /* cache freshness is managed at the repo/SessionCache layer */ }

    private suspend fun load() {
        try {
            val todayProgress = repo.getTodayProgress()   // cache-first
            val history       = repo.getHistory(30)        // cache-first with 1h TTL
            val ranking       = repo.getRankingPosition()  // uses cached ranking if available
            val progressData  = buildProgressWindow(history, todayProgress)
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
        viewModelScope.launch {
            try {
                repo.logPoints(10, "TIMER")  // bumps SessionCache.todayProgress/userProfile in place
                appPrefs.timerPointsDate = todayStr
                load()  // re-fetches fresh todayProgress, updates UiState
                // TODO: move notification sending to NotificationRepository so ProgressViewModel
                //       doesn't depend on a concrete Supabase class
                SupabaseNotificationRepository.insertForCurrentUser(
                    type  = "TIMER_DONE",
                    title = "¡Temporizador completado! ⏱",
                    body  = "Has completado una sesión de concentración y ganado 10 pts"
                )
            } catch (_: Exception) { }
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
