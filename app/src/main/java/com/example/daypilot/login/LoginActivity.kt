package com.example.daypilot.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.MainActivity
import com.example.daypilot.ui.theme.DayPilotTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DayPilotTheme {
                LoginScreen(
                    onLoginClick = {
                        startActivity(Intent(this, MainActivity::class.java))
                    },
                    onRegisterClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    }
                )
            }
        }
    }
}