package com.example.daypilot.main.mainZone.task

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
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

            DayPilotTheme(darkTheme = darkTheme) {
                val colorScheme = MaterialTheme.colorScheme
                val view = LocalView.current

                if (!view.isInEditMode) {
                    SideEffect {
                        // Fondo de barras
                        window.statusBarColor = colorScheme.background.toArgb()
                        window.navigationBarColor = colorScheme.background.toArgb()

                        // Iconos claros/oscursos según tema
                        WindowInsetsControllerCompat(window, window.decorView).apply {
                            isAppearanceLightStatusBars = !darkTheme
                            isAppearanceLightNavigationBars = !darkTheme
                        }
                    }
                }

                TaskScreen(
                    uid = user.uid,
                    taskRepo = taskRepo,
                    authRepo = authRepo,
                    onBack = { finish() }
                )
            }
        }
    }
}
