package com.example.daypilot.main.addFriend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme

class AddFriendActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = authRepo.currentUser
        if (currentUser == null) {
            finish()
            return
        }

        setContent {
            val darkTheme = sessionManager.isDarkModeEnabled()
            DayPilotTheme (darkTheme = darkTheme) {
                AddFriendScreen(
                    authRepo = authRepo,
                    currentUid = currentUser.uid,
                    onBack = { finish() }
                )
            }
        }
    }
}
