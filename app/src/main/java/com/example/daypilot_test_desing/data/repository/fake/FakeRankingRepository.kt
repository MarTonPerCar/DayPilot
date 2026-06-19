package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.model.RankingData
import com.example.daypilot_test_desing.data.repository.RankingRepository

object FakeRankingRepository : RankingRepository {
    private val ranking = listOf(
        RankingData("f3", "Sara López",   740, 21),
        RankingData("f1", "Ana García",   520, 14),
        RankingData("me", "Mario",        480, 7),
        RankingData("f2", "Luis Pérez",   310, 5),
        RankingData("f4", "Pedro Martín", 290, 3)
    )

    override fun getRanking()              = ranking
    override fun getCurrentUserId()        = "me"
    override fun getCurrentUserPosition()  = 3
    override fun getCurrentUserPoints()    = 480
    override fun getCurrentUserStreak()    = 7
}
