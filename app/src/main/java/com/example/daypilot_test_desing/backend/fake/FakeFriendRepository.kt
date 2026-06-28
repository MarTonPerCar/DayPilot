package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.FriendData
import com.example.daypilot_test_desing.backend.model.FriendWeeklySummary
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.model.SearchUserData
import com.example.daypilot_test_desing.backend.repository.FriendRepository

object FakeFriendRepository : FriendRepository {
    private val friends = mutableListOf(
        FriendData("f1", "Ana García",  "ana@example.com",   520, 14,
            weeklySummary = FriendWeeklySummary(180, 28, 52000, 14)),
        FriendData("f2", "Luis Pérez",  "luis@example.com",  310, 5,
            weeklySummary = FriendWeeklySummary(95,  15, 31000, 5)),
        FriendData("f3", "Sara López",  "sara@example.com",  740, 21,
            weeklySummary = FriendWeeklySummary(240, 35, 68000, 21)),
        FriendData("f4", "Pedro Martín","pedro@example.com", 290, 3,
            weeklySummary = FriendWeeklySummary(80,  12, 22000, 3))
    )

    private val requests = mutableListOf(
        FriendData("r1", "Elena Ruiz",  "elena@example.com",  150, 2),
        FriendData("r2", "Carlos Díaz", "carlos@example.com", 420, 9)
    )

    private val allUsers = listOf(
        SearchUserData("u1", "Marta Sanz",    "marta@example.com",   630, 18),
        SearchUserData("u2", "Jorge Blanco",  "jorge@example.com",   210, 4),
        SearchUserData("u3", "Isabel Torres", "isabel@example.com",  880, 30),
        SearchUserData("u4", "David Mora",    "david@example.com",   340, 7),
        SearchUserData("u5", "Lucía Castro",  "lucia@example.com",   590, 15)
    )

    override fun getFriends(): List<FriendData>  = friends.toList()
    override fun getFriendRequests(): List<FriendData> = requests.toList()

    override fun acceptRequest(userId: String) {
        val req = requests.find { it.id == userId } ?: return
        requests.remove(req)
        friends.add(req.copy(
            weeklySummary = FriendWeeklySummary(
                totalPoints    = 0,
                tasksCompleted = 0,
                totalSteps     = 0,
                bestStreak     = req.streak
            )
        ))
    }

    override fun rejectRequest(userId: String) { requests.removeAll { it.id == userId } }

    override fun reactToFriend(userId: String, reaction: ReactionType) {
        val idx = friends.indexOfFirst { it.id == userId }
        if (idx >= 0) {
            val summary = friends[idx].weeklySummary ?: FriendWeeklySummary(0, 0, 0, 0)
            friends[idx] = friends[idx].copy(weeklySummary = summary.copy(myReaction = reaction))
        }
    }

    override fun searchUsers(query: String): List<SearchUserData> =
        if (query.isBlank()) emptyList()
        else allUsers.filter { it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true) }

    override fun addFriend(userId: String) {
        val user = allUsers.find { it.id == userId } ?: return
        if (friends.none { it.id == userId } && requests.none { it.id == userId }) {
            friends.add(FriendData(
                id            = user.id,
                name          = user.name,
                email         = user.email,
                points        = user.points,
                streak        = user.streak,
                avatarUrl     = user.avatarUrl,
                weeklySummary = FriendWeeklySummary(0, 0, 0, user.streak)
            ))
        }
    }

    override fun removeFriend(userId: String) { friends.removeAll { it.id == userId } }
}
