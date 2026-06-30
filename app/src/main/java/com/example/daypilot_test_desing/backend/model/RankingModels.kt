package com.example.daypilot_test_desing.backend.model

data class RankingData(
    val id: String,
    val name: String,
    val points: Int,
    val streak: Int,
    val level: Int = 1,
    val avatarUrl: String? = null
)
