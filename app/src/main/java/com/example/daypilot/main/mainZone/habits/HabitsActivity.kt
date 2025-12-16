package com.example.daypilot.main.mainZone.habits

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.main.mainZone.habits.steps.StepsViewModel
import com.example.daypilot.main.mainZone.habits.steps.StepsViewModelFactory
import com.example.daypilot.ui.theme.DayPilotTheme
import java.time.ZoneId

class HabitsActivity : ComponentActivity() {

    private val authRepo = AuthRepository()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sessionManager = SessionManager(this)
        val uid = authRepo.currentUser?.uid
        val zoneId = ZoneId.systemDefault()

        val stepsVm: StepsViewModel? = uid?.let {
            ViewModelProvider(
                this,
                StepsViewModelFactory(applicationContext, it, zoneId)
            )[StepsViewModel::class.java]
        }

        setContent {
            val darkTheme = sessionManager.isDarkModeEnabled()
            DayPilotTheme(darkTheme = darkTheme) {
                val colorScheme = MaterialTheme.colorScheme

                SideEffect {
                    window.statusBarColor = colorScheme.background.toArgb()
                    window.navigationBarColor = colorScheme.background.toArgb()
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                var hasStepsPermission by remember { mutableStateOf(Build.VERSION.SDK_INT < 29) }
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted -> hasStepsPermission = granted }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= 29) {
                        launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }

                LaunchedEffect(hasStepsPermission, stepsVm) {
                    if (hasStepsPermission) stepsVm?.start()
                }

                val stepsUi = stepsVm?.ui?.collectAsState()?.value

                HabitsHubScreen(
                    onBack = { finish() },

                    stepsUi = stepsUi,
                    hasStepsPermission = hasStepsPermission,
                    onRequestStepsPermission = { launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                    onOpenSteps = {
                        startActivity(Intent(this, com.example.daypilot.main.mainZone.habits.steps.StepsActivity::class.java))
                    },
                    onChangeStepsGoal = { newGoal -> stepsVm?.setGoal(newGoal) },
                    onOpenTechHealth = {
                        startActivity(Intent(this, com.example.daypilot.main.mainZone.habits.tech.TechHealthActivity::class.java))
                    },
                    onOpenReminders = {
                        startActivity(Intent(this, com.example.daypilot.main.mainZone.habits.reminders.RemindersActivity::class.java))
                    }
                )
            }
        }
    }
}