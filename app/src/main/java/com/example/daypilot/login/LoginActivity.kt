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

class LoginActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DayPilotTheme {
                LoginScreen(
                    onLoginClick = { email, password ->
                        loginUser(email, password)
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        lifecycleScope.launch {

            val result = authRepo.login(email, password)

            if (result.isSuccess) {
                Toast.makeText(this@LoginActivity, "Bienvenido", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()

            } else {
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${result.exceptionOrNull()?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}