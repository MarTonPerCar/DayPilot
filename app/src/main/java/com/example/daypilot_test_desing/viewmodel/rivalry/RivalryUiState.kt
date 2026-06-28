package com.example.daypilot_test_desing.viewmodel.rivalry

import com.example.daypilot_test_desing.backend.model.RankingData

data class RivalryUiState(
    val currentUserName: String = "",
    val currentUserId: String = "",
    val currentUserPosition: Int = 0,
    val currentUserPoints: Int = 0,
    val currentUserStreak: Int = 0,
    val ranking: List<RankingData> = emptyList()
)
