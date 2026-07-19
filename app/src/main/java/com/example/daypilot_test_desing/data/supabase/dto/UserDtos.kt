package com.example.daypilot_test_desing.data.supabase.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// created_at is DB-default (don't include it); there is no member_since column.
@Serializable
data class NewUserDto(
    val id: String,
    val email: String,
    val name: String,
    val username: String,
    @SerialName("username_lower") val usernameLower: String,
    val region: String
)
