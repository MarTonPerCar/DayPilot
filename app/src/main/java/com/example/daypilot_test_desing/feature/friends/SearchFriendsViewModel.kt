package com.example.daypilot_test_desing.feature.friends

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.connectivity.ConnectivityState
import com.example.daypilot_test_desing.core.connectivity.isConnectivityError
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SearchFriendsViewModel(private val repo: FriendRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchFriendsUiState())
    val uiState: StateFlow<SearchFriendsUiState> = _uiState.asStateFlow()

    // Cached once on init; accurate for the lifetime of this search session
    private var friendIds: Set<String> = emptySet()
    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            val sentIds = try { repo.getPendingSentRequestUserIds() } catch (e: Exception) {
                Log.e(TAG, "Failed to load pending sent request ids", e)
                emptyList()
            }
            val fIds = try { repo.getFriendIds() } catch (e: Exception) {
                Log.e(TAG, "Failed to load friend ids", e)
                emptyList()
            }
            friendIds = fIds.toSet()
            _uiState.update { it.copy(sentRequestUserIds = sentIds.toSet()) }
        }
    }

    fun search(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            _uiState.update { it.copy(isLoading = true) }
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            try {
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
            } catch (e: Exception) {
                Log.e(TAG, "Failed to search users for '$query'", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun addFriend(userId: String) {
        val originalResults = _uiState.value.searchResults
        val originalSentIds = _uiState.value.sentRequestUserIds
        val newSentIds = originalSentIds + userId
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
            if (!ConnectivityState.ensureOnline()) {
                _uiState.update { state ->
                    state.copy(requestJustSent = false, sentRequestUserIds = originalSentIds, searchResults = originalResults)
                }
                return@launch
            }
            try {
                repo.addFriend(userId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send friend request to $userId", e)
                if (isConnectivityError(e)) ConnectivityState.setOffline(true)
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
        private const val TAG = "SearchFriendsViewModel"

        fun factory(repo: FriendRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SearchFriendsViewModel(repo) as T
            }
    }
}
