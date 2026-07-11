package com.example.daypilot_test_desing.feature.progress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.buildProgressWindow
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
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
                timerCompletedToday = todayProgress.timerPoints > 0
            )
            subscribeToRealtimeOnce()
        } catch (_: Exception) { }
    }

    // daily_progress has no TTL in SessionCache (write-through only), so without this,
    // a change made from another device/session would never be picked up here until the
    // date rolls over or a local action happens to overwrite the cache.
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

            // user_daily_log has a 1h TTL in SessionCache — force a real refetch
            // instead of serving the stale 30-day history/streak chart.
            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "user_daily_log"
                filter("user_id", FilterOperator.EQ, userId)
            }.onEach {
                SessionCache.weeklyHistory.value    = null
                SessionCache.weeklyHistoryFetchedAt = 0L
                load()
            }.launchIn(viewModelScope)

            // supabase-kt gives up and settles at UNSUBSCRIBED for good after enough
            // failed rejoin attempts (e.g. a stale JWT) — rebuild instead of leaving
            // progress sync dead for the rest of the session.
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
            try {
                val awarded = repo.completeTimerSession()  // server-side gated via habits_daily
                if (!awarded) return@launch
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
