package com.example.daypilot_test_desing.feature.rivalry

import com.example.daypilot_test_desing.core.data.model.RankingData

data class RivalryUiState(
    val currentUserName: String = "",
    val currentUserId: String = "",
    val currentUserPosition: Int = 0,
    val currentUserPoints: Int = 0,
    val currentUserStreak: Int = 0,
    val currentUserLevel: Int = 1,
    val ranking: List<RankingData> = emptyList()
)
