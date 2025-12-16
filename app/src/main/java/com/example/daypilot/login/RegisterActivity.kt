package com.example.daypilot.login

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        setContent {
            var isLoading by remember { mutableStateOf(false) }
            val darkTheme = sessionManager.isDarkModeEnabled()

            DayPilotTheme(darkTheme = darkTheme) {
                RegisterScreen(
                    darkTheme = darkTheme,
                    onRegisterClick = { name, username, email, password, regionZoneId ->
                        isLoading = true
                        lifecycleScope.launch {
                            try {
                                val available = authRepo.isUsernameAvailable(username)
                                if (!available) {
                                    showError("El nombre de usuario ya está en uso.")
                                    return@launch
                                }

                                val result = authRepo.register(email, password)
                                val user = result.getOrNull()

                                if (result.isSuccess && user != null) {
                                    authRepo.saveUserProfile(
                                        uid = user.uid,
                                        name = name,
                                        email = email,
                                        username = username,
                                        region = regionZoneId
                                    )
                                    finish()
                                } else {
                                    showError(
                                        result.exceptionOrNull()?.localizedMessage
                                            ?: "No se pudo crear el usuario."
                                    )
                                }
                            } catch (e: Exception) {
                                showError(e.localizedMessage ?: "Error durante el registro.")
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    onBackToLogin = { finish() },
                    isLoading = isLoading
                )
            }
        }
    }

    private fun showError(message: String) {
        AlertDialog.Builder(this@RegisterActivity)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}