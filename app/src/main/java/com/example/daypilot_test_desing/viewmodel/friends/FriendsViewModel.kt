package com.example.daypilot_test_desing.viewmodel.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(FriendsUiState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    fun refresh() { viewModelScope.launch { load() } }

    private suspend fun load() {
        try {
            _uiState.value = FriendsUiState(
                friends        = repo.getFriends(),
                friendRequests = repo.getFriendRequests()
            )
        } catch (_: Exception) { }
    }

    fun acceptRequest(userId: String) {
        viewModelScope.launch {
            repo.acceptRequest(userId)
            load()
        }
    }

    fun rejectRequest(userId: String) {
        viewModelScope.launch {
            repo.rejectRequest(userId)
            load()
        }
    }

    fun reactToFriend(userId: String, reaction: ReactionType) {
        viewModelScope.launch {
            repo.reactToFriend(userId, reaction)
            load()
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
