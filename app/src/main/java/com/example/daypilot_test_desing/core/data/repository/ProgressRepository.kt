package com.example.daypilot_test_desing.core.data.repository

import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto

interface ProgressRepository {
    suspend fun getTodayProgress(): DailyProgressDto
    suspend fun getHistory(days: Int = 7): List<DailyLogDto>
    suspend fun logPoints(points: Int, source: String)
    suspend fun getRankingPosition(): Int
}
