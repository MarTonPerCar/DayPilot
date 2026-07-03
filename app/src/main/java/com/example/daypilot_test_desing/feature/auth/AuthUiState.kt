package com.example.daypilot_test_desing.feature.auth

data class AuthUiState(
    val loginLoading: Boolean = false,
    val loginError: String = "",
    val registerLoading: Boolean = false,
    val registerError: String = "",
    val resetLoading: Boolean = false,
    val resetError: String = "",
    val resetSent: Boolean = false
)
