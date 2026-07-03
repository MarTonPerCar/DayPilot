package com.example.daypilot_test_desing.feature.friends

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.core.data.model.SearchUserData

data class SearchFriendsUiState(
    val searchResults: List<SearchUserData> = emptyList(),
    val isLoading: Boolean = false,
    val requestJustSent: Boolean = false,
    val sentRequestUserIds: Set<String> = emptySet(),
    @StringRes val userMessage: Int? = null
)
