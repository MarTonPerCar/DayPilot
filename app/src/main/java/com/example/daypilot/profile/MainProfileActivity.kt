package com.example.daypilot.profile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.MainDatabase.SessionManager
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.ui.theme.DayPilotTheme

class MainProfileActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)

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
            DayPilotTheme {
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