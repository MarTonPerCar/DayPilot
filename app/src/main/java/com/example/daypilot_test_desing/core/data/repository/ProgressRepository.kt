package com.example.daypilot_test_desing.core.data.repository

import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto

interface ProgressRepository {
    suspend fun getTodayProgress(): DailyProgressDto
    suspend fun getHistory(days: Int = 7): List<DailyLogDto>
    suspend fun logPoints(points: Int, source: String)
    suspend fun getRankingPosition(): Int

    /** Awards the once-per-day timer bonus if not already claimed today
     *  (checked server-side via habits_daily.timer_point_earned, not a local
     *  pref, so it's correct across devices/apps). Returns true if newly
     *  awarded. */
    suspend fun completeTimerSession(): Boolean
}
