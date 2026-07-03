package com.example.daypilot_test_desing.core.data.model

data class SearchUserData(
    val id: String,
    val name: String,
    val email: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null,
    val hasPendingRequest: Boolean = false
)
