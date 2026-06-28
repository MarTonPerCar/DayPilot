package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.RankingData
import com.example.daypilot_test_desing.backend.repository.RankingRepository

object FakeRankingRepository : RankingRepository {

    private fun buildRanking(): List<RankingData> {
        val user    = FakeUserRepository.getCurrentUserSync()
        val me      = RankingData("me", user.name, FakeProgressRepository.getMonthlyPoints(), user.currentStreak)
        val friends = FakeFriendRepository.getFriendsSync().map {
            RankingData(it.id, it.name, it.points, it.streak, it.avatarUrl)
        }
        return (listOf(me) + friends).sortedByDescending { it.points }
    }

    override suspend fun getRanking(): List<RankingData> = buildRanking()

    override suspend fun getCurrentUserId(): String = "me"

    override suspend fun getCurrentUserPosition(): Int {
        val ranking = buildRanking()
        val idx = ranking.indexOfFirst { it.id == "me" }
        return if (idx >= 0) idx + 1 else ranking.size + 1
    }

    override suspend fun getCurrentUserPoints(): Int = FakeProgressRepository.getMonthlyPoints()

    override suspend fun getCurrentUserStreak(): Int = FakeUserRepository.getCurrentUserSync().currentStreak

    // Non-interface sync method — used by FakeProgressRepository
    fun getCurrentUserPositionSync(): Int {
        val ranking = buildRanking()
        val idx = ranking.indexOfFirst { it.id == "me" }
        return if (idx >= 0) idx + 1 else ranking.size + 1
    }
}
