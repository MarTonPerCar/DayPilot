package com.example.daypilot_test_desing.core.cache

import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.RankingData
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import kotlinx.coroutines.flow.MutableStateFlow

// Repos must invalidate their slot after any write, or other screens see stale data.
object SessionCache {

    val todayProgress = MutableStateFlow<DailyProgressDto?>(null)
    val tasks         = MutableStateFlow<List<CalendarTaskData>?>(null)
    val userProfile   = MutableStateFlow<UserProfile?>(null)

    val weeklyHistory           = MutableStateFlow<List<DailyLogDto>?>(null)
    @Volatile var weeklyHistoryFetchedAt = 0L

    val friends                 = MutableStateFlow<List<FriendData>?>(null)
    @Volatile var friendsFetchedAt       = 0L

    val ranking                 = MutableStateFlow<List<RankingData>?>(null)
    @Volatile var rankingFetchedAt       = 0L

    const val SOCIAL_TTL_MS  = 5L  * 60_000L
    const val HISTORY_TTL_MS = 60L * 60_000L

    fun clear() {
        todayProgress.value    = null
        tasks.value            = null
        userProfile.value      = null
        weeklyHistory.value    = null
        weeklyHistoryFetchedAt = 0L
        friends.value          = null
        friendsFetchedAt       = 0L
        ranking.value          = null
        rankingFetchedAt       = 0L
    }
}
