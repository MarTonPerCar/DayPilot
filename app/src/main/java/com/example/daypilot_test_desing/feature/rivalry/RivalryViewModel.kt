package com.example.daypilot_test_desing.feature.rivalry

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.local.FriendStatsBroadcast
import com.example.daypilot_test_desing.core.data.repository.RankingRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class RivalryViewModel(private val repo: RankingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RivalryUiState())
    val uiState: StateFlow<RivalryUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null
    private var refreshing = false
    private val onFriendStatsChanged: () -> Unit = { refreshFromRealtime() }

    // Serializes concurrent load() calls (init, awaitLoad, realtime triggers) so a
    // slower failed load can't overwrite a faster successful one.
    private val loadMutex = Mutex()

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun invalidate() {
        SessionCache.ranking.value    = null
        SessionCache.rankingFetchedAt = 0L
    }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    // friends_ranking is a VIEW and never emits its own Realtime events, so this
    // watches the base `friends` table plus the shared friend-stats broadcast channel
    // (same instance FriendsViewModel listens to — don't create a second one).
    private fun subscribeToRealtimeOnce() {
        if (realtimeChannel != null) return
        val uid = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            val channel = supabase.channel("ranking-$uid")

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "friends"
                filter("requester_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "friends"
                filter("receiver_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.subscribe()
            realtimeChannel = channel
        }

        FriendStatsBroadcast.addListener(onFriendStatsChanged)
    }

    private fun refreshFromRealtime() {
        if (refreshing) return
        refreshing = true
        invalidate() // ranking is cache-first with a 5min TTL — force a real refetch
        viewModelScope.launch {
            try {
                load()
            } finally {
                refreshing = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { runCatching { realtimeChannel?.unsubscribe() } }
        FriendStatsBroadcast.removeListener(onFriendStatsChanged)
    }

    private suspend fun load(): Boolean = loadMutex.withLock {
        try {
            val ranking  = repo.getRanking()
            val uid      = repo.getCurrentUserId()
            val me       = ranking.firstOrNull { it.id == uid }
                           ?: repo.getCurrentUserData()
            val position = ranking.indexOfFirst { it.id == uid }
                           .let { if (it >= 0) it + 1 else ranking.size + 1 }
            _uiState.value = RivalryUiState(
                currentUserName     = me?.name ?: "",
                currentUserId       = uid,
                currentUserPosition = position,
                currentUserPoints   = me?.points ?: 0,
                currentUserStreak   = me?.streak ?: 0,
                currentUserLevel    = me?.level ?: 1,
                ranking             = ranking
            )
            subscribeToRealtimeOnce()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load rivalry data", e)
            false
        }
    }

    companion object {
        private const val TAG = "RivalryViewModel"

        fun factory(repo: RankingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RivalryViewModel(repo) as T
            }
    }
}
