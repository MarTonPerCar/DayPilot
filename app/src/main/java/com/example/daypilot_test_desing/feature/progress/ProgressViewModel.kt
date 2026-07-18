package com.example.daypilot_test_desing.feature.progress

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.buildProgressWindow
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

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun invalidate() { /* cache freshness is managed at the repo/SessionCache layer */ }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean {
        return try {
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
                timerCompletedToday = todayProgress.timerPoints > 0
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load progress data", e)
            false
        }
    }

    fun recordTimerComplete() {
        viewModelScope.launch {
            try {
                val awarded = repo.completeTimerSession()  // server-side gated via habits_daily
                if (!awarded) return@launch
                load()  // re-fetches fresh todayProgress, updates UiState
                // TIMER_DONE notification is now inserted by a Supabase DB trigger.
            } catch (_: Exception) { }
        }
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    companion object {
        private const val TAG = "ProgressViewModel"

        fun factory(application: Application, repo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProgressViewModel(application, repo) as T
            }
    }
}
