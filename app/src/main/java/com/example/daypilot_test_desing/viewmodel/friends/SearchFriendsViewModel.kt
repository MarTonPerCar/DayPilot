package com.example.daypilot_test_desing.viewmodel.friends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.repository.FriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchFriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchFriendsUiState())
    val uiState: StateFlow<SearchFriendsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadSentRequests() }
    }

    private suspend fun loadSentRequests() {
        val ids = try { repo.getPendingSentRequestUserIds() } catch (_: Exception) { emptyList() }
        _uiState.update { it.copy(sentRequestUserIds = ids.toSet()) }
    }

    fun search(query: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val results = repo.searchUsers(query)
                val sentIds = _uiState.value.sentRequestUserIds
                _uiState.update {
                    it.copy(
                        searchResults = results.map { r -> r.copy(hasPendingRequest = r.id in sentIds) },
                        isLoading = false
                    )
                }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addFriend(userId: String) {
        viewModelScope.launch {
            try {
                repo.addFriend(userId)
                val newSentIds = _uiState.value.sentRequestUserIds + userId
                _uiState.update { state ->
                    state.copy(
                        requestJustSent = true,
                        sentRequestUserIds = newSentIds,
                        searchResults = state.searchResults.map { r ->
                            if (r.id == userId) r.copy(hasPendingRequest = true) else r
                        }
                    )
                }
            } catch (_: Exception) { }
        }
    }

    fun dismissConfirmation() {
        _uiState.update { it.copy(requestJustSent = false) }
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
