package com.example.daypilot_test_desing.feature.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    private suspend fun load() {
        try {
            _uiState.update { current ->
                current.copy(
                    friends        = repo.getFriends(),         // cache-first with 5min TTL
                    friendRequests = repo.getFriendRequests()   // always fresh
                )
            }
        } catch (_: Exception) { }
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
                // Persisted to the DB — the always-on realtime subscription delivers it to
                // NotificationHub, so adding it locally too would double it up.
                SupabaseNotificationRepository.insertForCurrentUser(
                    type  = "FRIEND_ACCEPTED",
                    title = "Nueva amistad 🤝",
                    body  = "${request.name} es ahora tu amigo"
                )
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
                // repo.reactToFriend() already persists a "Reacción enviada" notification
                // for the current user and the always-on realtime subscription delivers it
                // to NotificationHub — adding it here too would double it up.
                repo.reactToFriend(userId, reaction)
                SessionCache.friends.value    = _uiState.value.friends
                SessionCache.friendsFetchedAt = System.currentTimeMillis()
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
        fun factory(repo: FriendRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FriendsViewModel(repo) as T
            }
    }
}
