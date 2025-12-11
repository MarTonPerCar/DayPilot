package com.example.daypilot.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.ui.theme.DayPilotTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class ResetPasswordActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val initialEmail = intent.getStringExtra("email") ?: ""

        setContent {
            var isLoading by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf<String?>(null) }
            var success by remember { mutableStateOf(false) }

            DayPilotTheme {
                ResetPasswordScreen(
                    initialEmail = initialEmail,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    success = success,
                    onBackClick = { finish() },
                    onSendClick = { email ->
                        isLoading = true
                        errorMessage = null
                        success = false

                        lifecycleScope.launch {
                            val result = authRepo.sendPasswordReset(email)
                            if (result.isSuccess) {
                                isLoading = false
                                success = true

                                // 👇 Espera 2 segundos y cierra la Activity
                                delay(2000)
                                finish()

                            } else {
                                isLoading = false
                                errorMessage =
                                    result.exceptionOrNull()?.localizedMessage
                                        ?: "Error al enviar el correo."
                            }
                        }
                    }
                )
            }
        }
    }
}