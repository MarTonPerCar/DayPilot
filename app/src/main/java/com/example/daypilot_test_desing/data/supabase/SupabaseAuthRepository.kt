package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.data.repository.AuthRepository
import com.example.daypilot_test_desing.core.data.repository.RegisterOutcome
import com.example.daypilot_test_desing.data.supabase.dto.NewUserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from

class SupabaseAuthRepository : AuthRepository {

    override suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        region: String
    ): RegisterOutcome {
        try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        } catch (e: Exception) {
            // could be a real existing account, or an orphan from a failed signup — sign in to tell them apart
            if (e.message?.contains("User already registered", ignoreCase = true) == true) {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
            } else throw e
        }
        val uid = supabase.auth.currentUserOrNull()?.id ?: return RegisterOutcome.PendingEmailConfirmation

        val hasProfile = supabase.from("users").select {
            filter { eq("id", uid) }
            limit(1)
        }.decodeList<UserDto>().isNotEmpty()
        if (hasProfile) return RegisterOutcome.AlreadyExists

        supabase.from("users").insert(
            NewUserDto(
                id = uid,
                email = email,
                name = name,
                username = username,
                usernameLower = username.lowercase(),
                region = region
            )
        )
        return RegisterOutcome.Success
    }

    override suspend fun sendResetEmail(email: String) {
        supabase.auth.resetPasswordForEmail(email)
    }
}
