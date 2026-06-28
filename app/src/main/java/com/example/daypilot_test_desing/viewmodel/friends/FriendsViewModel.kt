package com.example.daypilot_test_desing.viewmodel.friends

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.fake.FakeFriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FriendsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<FriendsUiState> = _uiState.asStateFlow()

    private fun buildState() = FriendsUiState(
        friends        = FakeFriendRepository.getFriends(),
        friendRequests = FakeFriendRepository.getFriendRequests()
    )

    fun refresh() { _uiState.value = buildState() }

    fun acceptRequest(userId: String) {
        FakeFriendRepository.acceptRequest(userId)
        _uiState.value = buildState()
    }

    fun rejectRequest(userId: String) {
        FakeFriendRepository.rejectRequest(userId)
        _uiState.value = buildState()
    }

    fun reactToFriend(userId: String, reaction: ReactionType) {
        FakeFriendRepository.reactToFriend(userId, reaction)
        _uiState.value = buildState()
    }

    fun removeFriend(userId: String) {
        FakeFriendRepository.removeFriend(userId)
        _uiState.value = buildState()
    }
}
