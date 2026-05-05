package com.example.daypilot_test_desing.ui.model

data class RankingData(
    val id: String,
    val name: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null
)
