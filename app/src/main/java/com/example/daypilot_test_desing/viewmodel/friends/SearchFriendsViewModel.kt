package com.example.daypilot_test_desing.viewmodel.friends

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeFriendRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchFriendsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SearchFriendsUiState())
    val uiState: StateFlow<SearchFriendsUiState> = _uiState.asStateFlow()

    fun search(query: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)
        val results = FakeFriendRepository.searchUsers(query)
        _uiState.value = SearchFriendsUiState(searchResults = results, isLoading = false)
    }

    fun addFriend(userId: String) {
        FakeFriendRepository.addFriend(userId)
    }
}
