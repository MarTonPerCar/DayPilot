package com.example.daypilot_test_desing.viewmodel.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.local.NotificationHub
import com.example.daypilot_test_desing.backend.model.NotificationType
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.repository.FriendRepository
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
                    friends        = repo.getFriends(),
                    friendRequests = repo.getFriendRequests()
                )
            }
        } catch (_: Exception) { }
    }

    fun acceptRequest(userId: String) {
        _uiState.update { it.copy(acceptingUserId = userId) }
        viewModelScope.launch {
            repo.acceptRequest(userId)
            load()
            _uiState.update { it.copy(acceptingUserId = null, justAcceptedRequest = true) }
            val name = _uiState.value.friends.firstOrNull { it.id == userId }?.name
            if (name != null) {
                NotificationHub.add(
                    title   = "Nueva amistad 🤝",
                    message = "$name es ahora tu amigo",
                    type    = NotificationType.SOCIAL
                )
            }
        }
    }

    fun clearJustAccepted() {
        _uiState.update { it.copy(justAcceptedRequest = false) }
    }

    fun rejectRequest(userId: String) {
        viewModelScope.launch {
            repo.rejectRequest(userId)
            load()
        }
    }

    fun reactToFriend(userId: String, reaction: ReactionType) {
        // Optimistic update: show checkmark immediately before the DB round-trip
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
            repo.reactToFriend(userId, reaction)
            val name = _uiState.value.friends.firstOrNull { it.id == userId }?.name
            val emoji = when (reaction) {
                ReactionType.FIRE   -> "🔥"
                ReactionType.CLAP   -> "👏"
                ReactionType.STRONG -> "💪"
                ReactionType.STAR   -> "⭐"
            }
            if (name != null) {
                NotificationHub.add(
                    title   = "Reacción enviada $emoji",
                    message = "Reaccionaste a la semana de $name",
                    type    = NotificationType.SOCIAL
                )
            }
            load() // re-sync with DB after sending
        }
    }

    fun removeFriend(userId: String) {
        viewModelScope.launch {
            repo.removeFriend(userId)
            load()
        }
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
