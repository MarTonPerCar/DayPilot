package com.example.daypilot_test_desing.core.data.repository

import com.example.daypilot_test_desing.core.data.model.RankingData

interface RankingRepository {
    suspend fun getRanking(): List<RankingData>
    suspend fun getCurrentUserId(): String
    suspend fun getCurrentUserData(): RankingData?
}
