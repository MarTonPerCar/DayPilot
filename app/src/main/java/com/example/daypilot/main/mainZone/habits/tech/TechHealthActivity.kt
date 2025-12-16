package com.example.daypilot.main.mainZone.habits.tech

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme

class TechHealthActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkNotifPermission()) {
            TechHealthForegroundService.start(applicationContext, tickMs = 5_000L)
        }

        val sessionManager = SessionManager(this)

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

                val vm: TechHealthViewModel = viewModel(
                    factory = TechHealthViewModelFactory(applicationContext)
                )

                var hasNotifPermission by remember { mutableStateOf(checkNotifPermission()) }
                val notifLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) {
                    hasNotifPermission = checkNotifPermission()
                    if (hasNotifPermission) {
                        TechHealthForegroundService.start(applicationContext, tickMs = 5_000L)
                    }
                }

                TechHealthScreen(
                    vm = vm,
                    hasNotifPermission = hasNotifPermission,
                    onRequestNotif = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onBack = { finish() }
                )
            }
        }
    }

    private fun checkNotifPermission(): Boolean {
        return if (Build.VERSION.SDK_INT < 33) true
        else ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}