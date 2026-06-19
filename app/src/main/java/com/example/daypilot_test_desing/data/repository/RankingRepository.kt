package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.RankingData

interface RankingRepository {
    fun getRanking(): List<RankingData>
    fun getCurrentUserId(): String
    fun getCurrentUserPosition(): Int
    fun getCurrentUserPoints(): Int
    fun getCurrentUserStreak(): Int
}
