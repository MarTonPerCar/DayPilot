package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.FriendData
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.model.SearchUserData

interface FriendRepository {
    suspend fun getFriends(): List<FriendData>
    suspend fun getFriendIds(): List<String>
    suspend fun getFriendRequests(): List<FriendData>
    suspend fun acceptRequest(userId: String)
    suspend fun rejectRequest(userId: String)
    suspend fun reactToFriend(userId: String, reaction: ReactionType)
    suspend fun searchUsers(query: String): List<SearchUserData>
    suspend fun addFriend(userId: String)
    suspend fun removeFriend(userId: String)
    suspend fun getPendingSentRequestUserIds(): List<String>
}
