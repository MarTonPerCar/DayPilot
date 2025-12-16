package com.example.daypilot.main.profile.mainProfile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.main.profile.settings.SettingsActivity
import com.example.daypilot.mainDatabase.SessionGuard
import com.example.daypilot.ui.theme.DayPilotTheme

class MainProfileActivity : ComponentActivity() {

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

        val firebaseUser = authRepo.currentUser
        if (firebaseUser == null) {
            // Si por lo que sea no hay usuario, te mando al login
            startActivity(
                Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }

        val uid = firebaseUser.uid
        val creationTs = firebaseUser.metadata?.creationTimestamp

        setContent {
            val darkTheme = sessionManager.isDarkModeEnabled()
            DayPilotTheme (darkTheme = darkTheme) {
                ProfileScreen(
                    authRepo = authRepo,
                    uid = uid,
                    accountCreationTimestamp = creationTs,
                    onOpenSettings = {
                        val intent = Intent(this, SettingsActivity::class.java)
                        startActivity(intent)
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}