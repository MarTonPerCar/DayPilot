package com.example.daypilot_test_desing.feature.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
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

class FriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null
    private var refreshing = false
    private val onFriendStatsChanged: () -> Unit = { refreshFromRealtime() }

    init {
        viewModelScope.launch { load() }
        // Fires when a FRIEND_REQUEST/FRIEND_ACCEPTED notification arrives via the
        // always-on notifications realtime channel (see NotificationsViewModel) —
        // kept alongside the direct subscriptions below as a second, overlapping
        // signal source; refreshFromRealtime()'s guard makes that harmless.
        NotificationHub.friendsShouldRefresh.onEach { refreshFromRealtime() }.launchIn(viewModelScope)
    }

    fun refresh(): Job = viewModelScope.launch { load() }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean {
        return try {
            _uiState.update { current ->
                current.copy(
                    friends        = repo.getFriends(),         // cache-first with 5min TTL
                    friendRequests = repo.getFriendRequests()   // always fresh
                )
            }
            subscribeToRealtimeOnce()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load friends data", e)
            false
        }
    }

    // friends/friend_requests/reactions have no OR-filter support in Postgres
    // Changes, so "requester or receiver" needs two separate subscriptions —
    // plus the shared friend-stats broadcast channel for a friend's own
    // streak/weekly-summary/points changes, which can't be filtered by my
    // own user_id since the changed row belongs to someone else.
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
        if (refreshing) return // a burst of changes shouldn't queue up overlapping fetches
        refreshing = true
        // getFriends() is cache-first with a 5min TTL — drop it so this actually
        // fetches fresh data instead of serving the stale cached list.
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
            try {
                repo.acceptRequest(userId)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
                SessionCache.ranking.value    = null
                SessionCache.rankingFetchedAt = 0L
                // FRIEND_ACCEPTED notification is now inserted by a Supabase DB trigger;
                // switching to the friends tab (justAcceptedRequest) is already the confirmation.
            } catch (e: Exception) {
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
            try {
                repo.rejectRequest(userId)
            } catch (e: Exception) {
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
            try {
                // REACTION notification to the friend is inserted by a Supabase DB trigger;
                // the "reaction sent" confirmation below is local-only, never stored.
                repo.reactToFriend(userId, reaction)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
                _uiState.update { it.copy(userMessage = R.string.friends_reaction_sent) }
            } catch (e: Exception) {
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
            try {
                repo.removeFriend(userId)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
                SessionCache.ranking.value    = null
                SessionCache.rankingFetchedAt = 0L
            } catch (e: Exception) {
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
