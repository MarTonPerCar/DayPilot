package com.example.daypilot.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.daypilot.main.MainActivity
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.MainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (sessionManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            var firebaseErrorCode by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            DayPilotTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        firebaseErrorCode = null
                        isLoading = true

                        loginUser(
                            email = email,
                            password = password,
                            onSuccess = {
                                isLoading = false
                                sessionManager.setLoggedIn(true)

                                startActivity(
                                    Intent(this@LoginActivity, MainActivity::class.java)
                                )
                                finish()
                            },
                            onError = { code ->
                                isLoading = false
                                firebaseErrorCode = code
                            }
                        )
                    },
                    onRegisterClick = {
                        firebaseErrorCode = null
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgotPasswordClick = { email ->
                        val intent = Intent(this@LoginActivity, ResetPasswordActivity::class.java).apply {
                            putExtra("email", email)
                        }
                        startActivity(intent)
                    },
                    firebaseErrorCode = firebaseErrorCode,
                    isLoading = isLoading
                )
            }
        }
    }

    private fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String?) -> Unit
    ) {
        lifecycleScope.launch {
            val result = authRepo.login(email, password)

            if (result.isSuccess) {
                onSuccess()
            } else {
                val exception = result.exceptionOrNull()
                val errorCode =
                    (exception as? FirebaseAuthException)?.errorCode
                        ?: exception?.message
                        ?: "ERROR_UNKNOWN"

                onError(errorCode)
            }
        }
    }
}