package com.example.daypilot_test_desing.feature.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
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
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load friends data", e)
            false
        }
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
