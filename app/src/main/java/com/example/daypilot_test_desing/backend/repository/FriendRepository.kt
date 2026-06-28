package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.FriendData
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.model.SearchUserData

interface FriendRepository {
    fun getFriends(): List<FriendData>
    fun getFriendRequests(): List<FriendData>
    fun acceptRequest(userId: String)
    fun rejectRequest(userId: String)
    fun reactToFriend(userId: String, reaction: ReactionType)
    fun searchUsers(query: String): List<SearchUserData>
    fun addFriend(userId: String)
    fun removeFriend(userId: String)
}
