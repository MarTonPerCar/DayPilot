package com.example.daypilot_test_desing.feature.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.repository.AuthRepository
import com.example.daypilot_test_desing.core.data.repository.RegisterOutcome
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {

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
                repo.login(email, password)
                _uiState.update { it.copy(loginLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                Log.w(TAG, "Login failed for $email", e)
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
        if (name.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(registerError = "Please fill in all fields.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(registerLoading = true, registerError = "") }
            try {
                when (repo.register(name, username, email, password, region)) {
                    RegisterOutcome.Success -> {
                        _uiState.update { it.copy(registerLoading = false) }
                        onSuccess()
                    }
                    RegisterOutcome.AlreadyExists -> {
                        _uiState.update {
                            it.copy(
                                registerLoading = false,
                                registerError = "An account with this email already exists."
                            )
                        }
                    }
                    RegisterOutcome.PendingEmailConfirmation -> {
                        _uiState.update {
                            it.copy(
                                registerLoading = false,
                                registerError = "Account created — check your email to confirm before logging in."
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Registration failed for $email", e)
                _uiState.update { it.copy(registerLoading = false, registerError = friendlyError(e)) }
            }
        }
    }

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(resetLoading = true, resetError = "", resetSent = false) }
            try {
                repo.sendResetEmail(email)
                _uiState.update { it.copy(resetLoading = false, resetSent = true) }
            } catch (e: Exception) {
                Log.w(TAG, "Password reset email failed for $email", e)
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

    companion object {
        private const val TAG = "AuthViewModel"

        fun factory(repo: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    AuthViewModel(repo) as T
            }
    }
}
