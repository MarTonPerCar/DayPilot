package com.example.daypilot.main.profile.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.main.mainZone.MainActivity
import com.example.daypilot.mainDatabase.SessionGuard
import com.example.daypilot.ui.theme.DayPilotTheme

class SettingsActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

        if (!SessionGuard.ensureValidSessionOrLogout(
                context = this,
                sessionManager = sessionManager,
                authRepo = authRepo
            )
        ) {
            finish()
            return
        }

        val user = authRepo.currentUser
        if (user == null) {
            finish()
            return
        }

        val notificationsInitial = sessionManager.areNotificationsEnabled()
        val languageInitial = sessionManager.getLanguage()

        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            val darkPref = sessionManager.isDarkModeEnabled()

            DayPilotTheme(darkTheme = darkPref) {
                val colorScheme = MaterialTheme.colorScheme
                val view = LocalView.current

                if (!view.isInEditMode) {
                    SideEffect {
                        // Colores de barras según el tema
                        window.statusBarColor = colorScheme.background.toArgb()
                        window.navigationBarColor = colorScheme.background.toArgb()

                        WindowInsetsControllerCompat(window, window.decorView).apply {
                            // Iconos oscuros en tema claro, claros en tema oscuro
                            isAppearanceLightStatusBars = !darkPref
                            isAppearanceLightNavigationBars = !darkPref
                        }
                    }
                }

                SettingsScreen(
                    authRepo = authRepo,
                    uid = user.uid,
                    isDarkModeInitial = darkPref,
                    notificationsInitial = notificationsInitial,
                    languageInitial = languageInitial,
                    onNotificationsChange = { enabled ->
                        sessionManager.setNotificationsEnabled(enabled)
                    },
                    onLanguageChange = { lang ->
                        sessionManager.setLanguage(lang)
                    },
                    onDarkModeChange = { enabled ->
                        sessionManager.setDarkModeEnabled(enabled)
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    },
                    onLogout = {
                        authRepo.logout()
                        sessionManager.logout()
                        val intent = Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        startActivity(intent)
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}