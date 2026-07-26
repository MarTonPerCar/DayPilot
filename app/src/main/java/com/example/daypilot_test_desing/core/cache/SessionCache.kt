package com.example.daypilot_test_desing.core.cache

import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.RankingData
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Repos must invalidate their slot after any write, or other screens see stale data.
object SessionCache {

    private val _todayProgress = MutableStateFlow<DailyProgressDto?>(null)
    val todayProgress: StateFlow<DailyProgressDto?> = _todayProgress.asStateFlow()
    fun setTodayProgress(value: DailyProgressDto?) { _todayProgress.value = value }

    private val _tasks = MutableStateFlow<List<CalendarTaskData>?>(null)
    val tasks: StateFlow<List<CalendarTaskData>?> = _tasks.asStateFlow()
    fun setTasks(value: List<CalendarTaskData>?) { _tasks.value = value }

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()
    fun setUserProfile(value: UserProfile?) { _userProfile.value = value }

    private val _weeklyHistory = MutableStateFlow<List<DailyLogDto>?>(null)
    val weeklyHistory: StateFlow<List<DailyLogDto>?> = _weeklyHistory.asStateFlow()
    fun setWeeklyHistory(value: List<DailyLogDto>?) { _weeklyHistory.value = value }
    @Volatile var weeklyHistoryFetchedAt = 0L

    private val _friends = MutableStateFlow<List<FriendData>?>(null)
    val friends: StateFlow<List<FriendData>?> = _friends.asStateFlow()
    fun setFriends(value: List<FriendData>?) { _friends.value = value }
    @Volatile var friendsFetchedAt       = 0L

    private val _ranking = MutableStateFlow<List<RankingData>?>(null)
    val ranking: StateFlow<List<RankingData>?> = _ranking.asStateFlow()
    fun setRanking(value: List<RankingData>?) { _ranking.value = value }
    @Volatile var rankingFetchedAt       = 0L

    const val SOCIAL_TTL_MS  = 5L  * 60_000L
    const val HISTORY_TTL_MS = 60L * 60_000L

    fun clear() {
        _todayProgress.value   = null
        _tasks.value           = null
        _userProfile.value     = null
        _weeklyHistory.value   = null
        weeklyHistoryFetchedAt = 0L
        _friends.value         = null
        friendsFetchedAt       = 0L
        _ranking.value         = null
        rankingFetchedAt       = 0L
    }
}
