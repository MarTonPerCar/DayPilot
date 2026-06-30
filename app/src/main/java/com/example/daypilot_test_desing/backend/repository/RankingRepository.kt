package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.RankingData

interface RankingRepository {
    suspend fun getRanking(): List<RankingData>
    suspend fun getCurrentUserId(): String
    suspend fun getCurrentUserData(): RankingData?
    suspend fun getCurrentUserPosition(): Int
    suspend fun getCurrentUserPoints(): Int
    suspend fun getCurrentUserStreak(): Int
}
