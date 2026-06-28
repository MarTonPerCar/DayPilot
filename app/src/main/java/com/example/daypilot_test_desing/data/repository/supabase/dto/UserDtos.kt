package com.example.daypilot_test_desing.data.repository.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload for INSERT into the public `users` table after sign-up.
 *
 * Confirmed columns: id, email, name, username, username_lower, region
 * created_at is set by the DB default — do not include it.
 * member_since does NOT exist — use created_at instead.
 */
@Serializable
data class NewUserDto(
    val id: String,
    val email: String,
    val name: String,
    val username: String,
    @SerialName("username_lower") val usernameLower: String,
    val region: String
)
