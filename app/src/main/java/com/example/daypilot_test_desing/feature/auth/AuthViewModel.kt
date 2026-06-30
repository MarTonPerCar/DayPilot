package com.example.daypilot_test_desing.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.supabase.dto.NewUserDto
import com.example.daypilot_test_desing.backend.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(loginError = "Please enter your email and password.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loginLoading = true, loginError = "") }
            try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                _uiState.update { it.copy(loginLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(loginLoading = false, loginError = friendlyError(e)) }
            }
        }
    }

    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        region: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(registerLoading = true, registerError = "") }
            try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                val uid = supabase.auth.currentUserOrNull()?.id
                if (uid != null) {
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
                    _uiState.update { it.copy(registerLoading = false) }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            registerLoading = false,
                            registerError = "Account created — check your email to confirm before logging in."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(registerLoading = false, registerError = friendlyError(e)) }
            }
        }
    }

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(resetLoading = true, resetError = "", resetSent = false) }
            try {
                supabase.auth.resetPasswordForEmail(email)
                _uiState.update { it.copy(resetLoading = false, resetSent = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(resetLoading = false, resetError = friendlyError(e)) }
            }
        }
    }

    fun clearResetState() {
        _uiState.update { it.copy(resetLoading = false, resetError = "", resetSent = false) }
    }

    private fun friendlyError(e: Exception): String {
        val raw = e.message ?: return "Unknown error"
        return when {
            raw.contains("Invalid login credentials", ignoreCase = true) -> "Incorrect email or password."
            raw.contains("Email not confirmed", ignoreCase = true)        -> "Please confirm your email first."
            raw.contains("User already registered", ignoreCase = true)    -> "An account with this email already exists."
            raw.contains("Password should be", ignoreCase = true)         -> "Password must be at least 6 characters."
            raw.contains("Unable to validate email", ignoreCase = true)   -> "Please enter a valid email address."
            else -> raw
        }
    }
}
