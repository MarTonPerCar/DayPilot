package com.example.daypilot_test_desing.core.data.repository

sealed interface RegisterOutcome {
    data object Success : RegisterOutcome
    data object AlreadyExists : RegisterOutcome
    data object PendingEmailConfirmation : RegisterOutcome
}

interface AuthRepository {
    suspend fun login(email: String, password: String)
    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        region: String
    ): RegisterOutcome
    suspend fun sendResetEmail(email: String)
}
