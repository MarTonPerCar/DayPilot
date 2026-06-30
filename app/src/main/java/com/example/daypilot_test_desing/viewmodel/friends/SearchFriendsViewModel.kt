package com.example.daypilot_test_desing.viewmodel.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.backend.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchFriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchFriendsUiState())
    val uiState: StateFlow<SearchFriendsUiState> = _uiState.asStateFlow()

    private var friendIds: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            val sentIds = try { repo.getPendingSentRequestUserIds() } catch (_: Exception) { emptyList() }
            val fIds    = try { repo.getFriendIds() }                 catch (_: Exception) { emptyList() }
            friendIds = fIds.toSet()
            _uiState.update { it.copy(sentRequestUserIds = sentIds.toSet()) }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                // Refresh friend IDs so removed friends appear again immediately
                friendIds = try { repo.getFriendIds() } catch (_: Exception) { emptyList() }.toSet()
                val results = repo.searchUsers(query)
                val sentIds = _uiState.value.sentRequestUserIds
                _uiState.update {
                    it.copy(
                        searchResults = results
                            .filter { r -> r.id !in friendIds }
                            .map { r -> r.copy(hasPendingRequest = r.id in sentIds) },
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addFriend(userId: String) {
        val originalResults = _uiState.value.searchResults
        val originalSentIds = _uiState.value.sentRequestUserIds
        val newSentIds = originalSentIds + userId
        // Mark pending and show confirmation before the API responds
        _uiState.update { state ->
            state.copy(
                requestJustSent    = true,
                sentRequestUserIds = newSentIds,
                searchResults      = state.searchResults.map { r ->
                    if (r.id == userId) r.copy(hasPendingRequest = true) else r
                }
            )
        }
        viewModelScope.launch {
            try {
                repo.addFriend(userId)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        requestJustSent    = false,
                        sentRequestUserIds = originalSentIds,
                        searchResults      = originalResults,
                        userMessage        = R.string.error_add_friend
                    )
                }
            }
        }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(requestJustSent = false) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun factory(repo: FriendRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SearchFriendsViewModel(repo) as T
            }
    }
}
