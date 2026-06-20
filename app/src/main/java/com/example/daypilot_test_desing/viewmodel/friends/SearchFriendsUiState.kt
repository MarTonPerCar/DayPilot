package com.example.daypilot_test_desing.viewmodel.friends

import com.example.daypilot_test_desing.data.model.SearchUserData

data class SearchFriendsUiState(
    val searchResults: List<SearchUserData> = emptyList(),
    val isLoading: Boolean = false
)
