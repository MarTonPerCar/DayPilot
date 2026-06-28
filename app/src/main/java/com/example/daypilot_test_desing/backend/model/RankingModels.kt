package com.example.daypilot_test_desing.backend.model

data class RankingData(
    val id: String,
    val name: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null
)
