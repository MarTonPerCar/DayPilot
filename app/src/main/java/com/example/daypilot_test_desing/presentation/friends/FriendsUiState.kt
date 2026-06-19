package com.example.daypilot_test_desing.presentation.friends

import com.example.daypilot_test_desing.data.model.FriendData

data class FriendsUiState(
    val friends: List<FriendData> = emptyList(),
    val friendRequests: List<FriendData> = emptyList()
)
