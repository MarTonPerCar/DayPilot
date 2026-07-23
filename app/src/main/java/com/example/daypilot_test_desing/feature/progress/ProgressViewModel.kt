package com.example.daypilot_test_desing.feature.progress

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.connectivity.ConnectivityState
import com.example.daypilot_test_desing.core.connectivity.isConnectivityError
import com.example.daypilot_test_desing.core.data.model.buildProgressWindow
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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

    private var realtimeChannel: RealtimeChannel? = null

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun invalidate() { /* cache freshness is managed at the repo/SessionCache layer */ }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean {
        if (!ConnectivityState.ensureOnline()) return false
        return try {
            val todayProgress = repo.getTodayProgress()
            val history       = repo.getHistory(30)
            val ranking       = repo.getRankingPosition()
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
            subscribeToRealtimeOnce()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load progress data", e)
            false
        }
    }

    // daily_progress is write-through with no TTL, so without realtime a change from
    // another device wouldn't surface here until the date rolls over.
    private fun subscribeToRealtimeOnce() {
        if (realtimeChannel != null) return
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        subscribeToRealtime(uid)
    }

    private fun subscribeToRealtime(userId: String) {
        viewModelScope.launch {
            val channel = supabase.channel("daily-progress-$userId")
            channel.postgresChangeFlow<PostgresAction.Update>(schema = "public") {
                table = "daily_progress"
                filter("user_id", FilterOperator.EQ, userId)
            }.onEach {
                SessionCache.todayProgress.value = null
                load()
            }.launchIn(viewModelScope)

            // Force a real refetch instead of serving the 1h-TTL'd stale history chart.
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "user_daily_log"
                filter("user_id", FilterOperator.EQ, userId)
            }.onEach {
                SessionCache.weeklyHistory.value    = null
                SessionCache.weeklyHistoryFetchedAt = 0L
                load()
            }.launchIn(viewModelScope)

            // supabase-kt settles at UNSUBSCRIBED for good after enough failed rejoin
            // attempts (e.g. a stale JWT) — rebuild instead of leaving progress sync dead.
            channel.status.onEach { status ->
                if (status == RealtimeChannel.Status.UNSUBSCRIBED && realtimeChannel === channel) {
                    delay(5_000)
                    if (realtimeChannel === channel) {
                        realtimeChannel = null
                        subscribeToRealtime(userId)
                    }
                }
            }.launchIn(viewModelScope)

            channel.subscribe()
            realtimeChannel = channel
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { runCatching { realtimeChannel?.unsubscribe() } }
    }

    fun recordTimerComplete() {
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) return@launch
            try {
                val awarded = repo.completeTimerSession()  // server-side gated via habits_daily
                if (!awarded) return@launch
                load()
                // TIMER_DONE notification is now inserted by a Supabase DB trigger.
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record timer completion", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
            }
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
