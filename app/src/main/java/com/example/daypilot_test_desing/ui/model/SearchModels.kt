package com.example.daypilot_test_desing.ui.model

data class SearchUserData(
    val id: String,
    val name: String,
    val email: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null
)
