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
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.ui.theme.DayPilotTheme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var isLoading by remember { mutableStateOf(false) }

            DayPilotTheme {
                RegisterScreen(
                    onRegisterClick = { name, username, email, password, regionZoneId ->
                        isLoading = true

                        lifecycleScope.launch {
                            try {
                                val available = authRepo.isUsernameAvailable(username)
                                if (!available) {
                                    isLoading = false
                                    showError("El nombre de usuario ya está en uso.")
                                    return@launch
                                }

                                val result = authRepo.register(email, password)

                                if (result.isSuccess) {
                                    val user = result.getOrNull()

                                    if (user != null) {
                                        try {
                                            authRepo.saveUserProfile(
                                                uid = user.uid,
                                                name = name,
                                                email = email,
                                                username = username,
                                                region = regionZoneId   // 👈 guardamos el ID real
                                            )
                                            finish()
                                        } catch (e: Exception) {
                                            showError(
                                                e.localizedMessage
                                                    ?: "Error guardando el perfil."
                                            )
                                        }
                                    } else {
                                        showError("No se pudo crear el usuario.")
                                    }
                                } else {
                                    val error = result.exceptionOrNull()
                                    showError(
                                        error?.localizedMessage
                                            ?: "Ocurrió un error inesperado."
                                    )
                                }
                            } catch (e: Exception) {
                                showError(
                                    e.localizedMessage ?: "Error durante el registro."
                                )
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