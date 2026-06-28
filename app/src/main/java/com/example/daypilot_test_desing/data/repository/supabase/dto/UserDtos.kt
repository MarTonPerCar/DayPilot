package com.example.daypilot_test_desing.data.repository.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Payload for INSERT into the public `users` table after sign-up. */
@Serializable
data class NewUserDto(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val region: String,
    @SerialName("member_since") val memberSince: String
)
