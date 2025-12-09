package com.example.daypilot.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.ui.theme.DayPilotTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DayPilotTheme {
                RegisterScreen(
                    onRegisterClick = {
                        finish() // vuelve al login
                    },
                    onBackToLogin = {
                        finish()
                    }
                )
            }
        }
    }
}