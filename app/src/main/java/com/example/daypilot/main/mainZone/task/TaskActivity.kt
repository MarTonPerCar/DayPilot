package com.example.daypilot.main.mainZone.task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.taskLogic.TaskRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme

class TaskActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var taskRepo: TaskRepository
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        taskRepo = TaskRepository(authRepo)

        val user = authRepo.currentUser
        if (user == null) {
            finish()
            return
        }

        val darkPref = sessionManager.isDarkModeEnabled()

        setContent {
            DayPilotTheme(darkTheme = darkPref) {
                TaskScreen(
                    uid = user.uid,
                    taskRepo = taskRepo,
                    onBack = { finish() }
                )
            }
        }
    }
}
