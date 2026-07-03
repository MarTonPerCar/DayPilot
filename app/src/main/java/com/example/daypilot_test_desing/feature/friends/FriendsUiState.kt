package com.example.daypilot_test_desing.feature.friends

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.core.data.model.FriendData

data class FriendsUiState(
    val friends: List<FriendData> = emptyList(),
    val friendRequests: List<FriendData> = emptyList(),
    val acceptingUserId: String? = null,
    val justAcceptedRequest: Boolean = false,
    @StringRes val userMessage: Int? = null
)
