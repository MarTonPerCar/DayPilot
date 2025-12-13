package com.example.daypilot.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.ui.theme.DayPilotTheme

class AddFriendActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = authRepo.currentUser
        if (currentUser == null) {
            finish()
            return
        }

        setContent {
            DayPilotTheme {
                AddFriendScreen(
                    authRepo = authRepo,
                    currentUid = currentUser.uid,
                    onBack = { finish() }
                )
            }
        }
    }
}
