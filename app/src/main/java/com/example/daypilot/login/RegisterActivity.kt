package com.example.daypilot.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.daypilot.MainActivity
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.ui.theme.DayPilotTheme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DayPilotTheme {
                RegisterScreen(
                    onRegisterClick = { name, email, password ->
                        registerUser(name, email, password)
                    },
                    onBackToLogin = { finish() }
                )
            }
        }
    }

    private fun registerUser(name: String, email: String, password: String) {
        lifecycleScope.launch {
            try {
                val result = authRepo.register(email, password)

                if (result.isSuccess) {
                    val user = result.getOrNull()
                    if (user != null) {
                        authRepo.saveUserProfile(user.uid, name, email)

                        Toast.makeText(this@RegisterActivity, "Cuenta creada", Toast.LENGTH_SHORT).show()

                        startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Error: ${result.exceptionOrNull()?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}