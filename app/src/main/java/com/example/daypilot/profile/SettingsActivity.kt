
package com.example.daypilot.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.MainDatabase.SessionManager
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.ui.theme.DayPilotTheme

class SettingsActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        setContent {
            DayPilotTheme {
                SettingsScreen(
                    authRepo = authRepo,
                    onLogout = {
                        authRepo.logout()
                        sessionManager.logout()
                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                        finish()
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}