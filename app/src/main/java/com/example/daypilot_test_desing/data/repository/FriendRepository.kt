package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.FriendData
import com.example.daypilot_test_desing.data.model.ReactionType
import com.example.daypilot_test_desing.data.model.SearchUserData

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
