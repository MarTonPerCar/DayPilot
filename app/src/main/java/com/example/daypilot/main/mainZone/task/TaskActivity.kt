package com.example.daypilot.main.mainZone.task

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.taskLogic.TaskRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme

class TaskActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var taskRepo: TaskRepository
    private lateinit var sessionManager: SessionManager

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sessionManager = SessionManager(this)
        taskRepo = TaskRepository()

        val user = authRepo.currentUser
        if (user == null) {
            finish()
            return
        }

        setContent {
            val darkTheme = sessionManager.isDarkModeEnabled()
            val openTaskId = intent.getStringExtra("openTaskId")

            val pointsRepo = remember { com.example.daypilot.firebaseLogic.pointsLogic.PointsRepository() }

            DayPilotTheme(darkTheme = darkTheme) {
                val colorScheme = MaterialTheme.colorScheme
                val view = LocalView.current

                if (!view.isInEditMode) {
                    SideEffect {
                        window.statusBarColor = colorScheme.background.toArgb()
                        window.navigationBarColor = colorScheme.background.toArgb()

                        WindowInsetsControllerCompat(window, window.decorView).apply {
                            isAppearanceLightStatusBars = !darkTheme
                            isAppearanceLightNavigationBars = !darkTheme
                        }
                    }
                }

                TaskScreen(
                    uid = user.uid,
                    taskRepo = taskRepo,
                    pointsRepo = pointsRepo,
                    openTaskId = openTaskId,
                    onBack = { finish() }
                )
            }
        }
    }
}
