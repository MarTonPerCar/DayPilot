package com.example.daypilot_test_desing.data.supabase

import android.util.Log
import com.example.daypilot_test_desing.core.data.repository.AuthRepository
import com.example.daypilot_test_desing.core.data.repository.RegisterOutcome
import com.example.daypilot_test_desing.data.supabase.dto.NewUserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class SupabaseAuthRepository : AuthRepository {

    companion object {
        private const val TAG = "SupabaseAuthRepository"
    }

    override suspend fun login(email: String, password: String) {
        supabase.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        // register() has no session to insert the profile row with (email confirmation
        // required) — this is the first point a confirmed user actually has one.
        supabase.auth.currentUserOrNull()?.let { ensureProfileExists(it) }
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
                data = buildJsonObject {
                    put("name", name)
                    put("username", username)
                    put("region", region)
                }
            }
        } catch (e: Exception) {
            // could be a real existing account, or an orphan from a failed signup — sign in to tell them apart
            if (e.message?.contains("User already registered", ignoreCase = true) == true) {
                try {
                    supabase.auth.signInWith(Email) {
                        this.email = email
                        this.password = password
                    }
                } catch (signInException: Exception) {
                    // Wrong password for an existing account — surface "already registered"
                    // instead of a confusing invalid-credentials error for what looked like sign-up.
                    Log.w(TAG, "register(): sign-in fallback failed for $email, treating as AlreadyExists", signInException)
                    return RegisterOutcome.AlreadyExists
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

    private suspend fun ensureProfileExists(user: UserInfo) {
        val hasProfile = supabase.from("users").select {
            filter { eq("id", user.id) }
            limit(1)
        }.decodeList<UserDto>().isNotEmpty()
        if (hasProfile) return

        val metadata = user.userMetadata
        val username = metadata?.get("username")?.jsonPrimitive?.contentOrNull ?: user.email?.substringBefore("@") ?: user.id
        supabase.from("users").insert(
            NewUserDto(
                id = user.id,
                email = user.email ?: "",
                name = metadata?.get("name")?.jsonPrimitive?.contentOrNull ?: username,
                username = username,
                usernameLower = username.lowercase(),
                region = metadata?.get("region")?.jsonPrimitive?.contentOrNull ?: ""
            )
        )
    }
}
