package com.example.daypilot_test_desing.feature.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.connectivity.ConnectivityState
import com.example.daypilot_test_desing.core.connectivity.isConnectivityError
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.local.FriendStatsBroadcast
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class FriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null
    private var refreshing = false
    private val onFriendStatsChanged: () -> Unit = { refreshFromRealtime() }

    // Serializes concurrent load() calls (init, awaitLoad, realtime triggers) so a
    // slower failed load can't overwrite a faster successful one.
    private val loadMutex = Mutex()

    init {
        Log.d(TAG, "init: starting first load()")
        viewModelScope.launch { load() }
        // Second, overlapping refresh signal alongside the direct subscriptions below —
        // refreshFromRealtime()'s guard makes the overlap harmless.
        NotificationHub.friendsShouldRefresh.onEach { refreshFromRealtime() }.launchIn(viewModelScope)
    }

    fun refresh(): Job = viewModelScope.launch { load() }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean = loadMutex.withLock {
        if (!ConnectivityState.ensureOnline()) return@withLock false
        try {
            val fetchedFriends  = repo.getFriends()         // cache-first with 5min TTL
            val fetchedRequests = repo.getFriendRequests()  // always fresh
            _uiState.update { it.copy(friends = fetchedFriends, friendRequests = fetchedRequests) }
            Log.d(TAG, "load(): ${fetchedFriends.size} friend(s), ${fetchedRequests.size} request(s)")
            subscribeToRealtimeOnce()
            true
        } catch (e: Exception) {
            // _uiState is left untouched so a transient failure doesn't wipe the last-good list.
            Log.e(TAG, "Failed to load friends data — keeping previously shown state", e)
            false
        }
    }

    // No OR-filter support in Postgres Changes, so "requester or receiver" needs two
    // subscriptions; the broadcast channel covers a friend's own stat changes, which
    // can't be filtered by my user_id since the row belongs to someone else.
    private fun subscribeToRealtimeOnce() {
        if (realtimeChannel != null) return
        val uid = supabase.auth.currentUserOrNull()?.id ?: return

        viewModelScope.launch {
            val channel = supabase.channel("friends-$uid")

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "friends"
                filter("requester_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "friends"
                filter("receiver_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "friend_requests"
                filter("to_user_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.postgresChangeFlow<PostgresAction>(schema = "public") {
                table = "reactions"
                filter("from_user_id", FilterOperator.EQ, uid)
            }.onEach { refreshFromRealtime() }.launchIn(viewModelScope)

            channel.subscribe()
            realtimeChannel = channel
        }

        FriendStatsBroadcast.addListener(onFriendStatsChanged)
    }

    private fun refreshFromRealtime() {
        if (refreshing) {
            Log.d(TAG, "refreshFromRealtime(): already refreshing, skipping duplicate trigger")
            return // a burst of changes shouldn't queue up overlapping fetches
        }
        Log.d(TAG, "refreshFromRealtime(): triggered")
        refreshing = true
        // Drop the cache slot so getFriends() fetches fresh instead of serving the stale TTL'd list.
        SessionCache.friends.value    = null
        SessionCache.friendsFetchedAt = 0L
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

    fun acceptRequest(userId: String) {
        val request = _uiState.value.friendRequests.find { it.id == userId } ?: return
        val originalRequests = _uiState.value.friendRequests
        val originalFriends  = _uiState.value.friends
        _uiState.update { state ->
            state.copy(
                friendRequests      = state.friendRequests.filter { it.id != userId },
                friends             = state.friends + request,
                justAcceptedRequest = true
            )
        }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state ->
                    state.copy(friendRequests = originalRequests, friends = originalFriends, justAcceptedRequest = false)
                }
                return@launch
            }
            try {
                repo.acceptRequest(userId)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
                SessionCache.ranking.value    = null
                SessionCache.rankingFetchedAt = 0L
                // FRIEND_ACCEPTED notification is inserted by a Supabase DB trigger, not here.
            } catch (e: Exception) {
                Log.e(TAG, "Failed to accept friend request from $userId", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(
                        friendRequests      = originalRequests,
                        friends             = originalFriends,
                        justAcceptedRequest = false,
                        userMessage         = R.string.error_accept_request
                    )
                }
            }
        }
    }

    fun clearJustAccepted() {
        _uiState.update { it.copy(justAcceptedRequest = false) }
    }

    fun rejectRequest(userId: String) {
        val originalRequests = _uiState.value.friendRequests
        _uiState.update { state -> state.copy(friendRequests = state.friendRequests.filter { it.id != userId }) }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state -> state.copy(friendRequests = originalRequests) }
                return@launch
            }
            try {
                repo.rejectRequest(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to reject friend request from $userId", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(friendRequests = originalRequests, userMessage = R.string.error_reject_request)
                }
            }
        }
    }

    fun reactToFriend(userId: String, reaction: ReactionType) {
        val originalFriends = _uiState.value.friends
        _uiState.update { state ->
            state.copy(
                friends = state.friends.map { friend ->
                    if (friend.id == userId)
                        friend.copy(weeklySummary = friend.weeklySummary?.copy(myReaction = reaction))
                    else
                        friend
                }
            )
        }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state -> state.copy(friends = originalFriends) }
                return@launch
            }
            try {
                // REACTION notification is inserted by a Supabase DB trigger; the
                // "reaction sent" confirmation below is local-only, never stored.
                repo.reactToFriend(userId, reaction)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
                _uiState.update { it.copy(userMessage = R.string.friends_reaction_sent) }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send reaction $reaction to $userId", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(friends = originalFriends, userMessage = R.string.error_react_friend)
                }
            }
        }
    }

    fun removeFriend(userId: String) {
        val originalFriends = _uiState.value.friends
        _uiState.update { state -> state.copy(friends = state.friends.filter { it.id != userId }) }
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state -> state.copy(friends = originalFriends) }
                return@launch
            }
            try {
                repo.removeFriend(userId)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
                SessionCache.ranking.value    = null
                SessionCache.rankingFetchedAt = 0L
            } catch (e: Exception) {
                Log.e(TAG, "Failed to remove friend $userId", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { state ->
                    state.copy(friends = originalFriends, userMessage = R.string.error_remove_friend)
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        private const val TAG = "FriendsViewModel"

        fun factory(repo: FriendRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FriendsViewModel(repo) as T
            }
    }
}
