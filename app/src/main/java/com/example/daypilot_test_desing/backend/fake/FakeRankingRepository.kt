package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.RankingData
import com.example.daypilot_test_desing.backend.repository.RankingRepository

object FakeRankingRepository : RankingRepository {

    override fun getRanking(): List<RankingData> {
        val user = FakeUserRepository.getCurrentUserSync()
        val me = RankingData("me", user.name, FakeProgressRepository.getMonthlyPoints(), user.currentStreak)
        val friends = FakeFriendRepository.getFriendsSync().map {
            RankingData(it.id, it.name, it.points, it.streak, it.avatarUrl)
        }
        return (listOf(me) + friends).sortedByDescending { it.points }
    }

    override fun getCurrentUserId() = "me"

    override fun getCurrentUserPosition(): Int {
        val idx = getRanking().indexOfFirst { it.id == "me" }
        return if (idx >= 0) idx + 1 else getRanking().size + 1
    }

    override fun getCurrentUserPoints() = FakeProgressRepository.getMonthlyPoints()

    override fun getCurrentUserStreak() = FakeUserRepository.getCurrentUserSync().currentStreak
}
