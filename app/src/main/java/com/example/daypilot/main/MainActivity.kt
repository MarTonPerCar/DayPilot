// com.example.daypilot.main.MainActivity.kt
package com.example.daypilot.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.MainDatabase.SessionManager
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.ui.theme.DayPilotTheme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        sessionManager = SessionManager(this)

        val user = authRepo.currentUser
        if (user == null) {
            goToLoginAndFinish()
            return
        }

        setContent {
            DayPilotTheme {
                val darkTheme = isSystemInDarkTheme()
                val colorScheme = MaterialTheme.colorScheme

                SideEffect {
                    window.statusBarColor = colorScheme.background.toArgb()
                    window.navigationBarColor = colorScheme.background.toArgb()

                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                MainScreen(
                    authRepo = authRepo,
                    sessionManager = sessionManager,
                    onLogoutToLogin = { goToLoginAndFinish() }
                )
            }
        }
    }

    private fun goToLoginAndFinish() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        // finish() no es estrictamente necesario por CLEAR_TASK, pero no molesta
        finish()
    }
}