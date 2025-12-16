// com.example.daypilot.main.MainActivity.kt
package com.example.daypilot.main.mainZone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.ui.theme.DayPilotTheme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.daypilot.firebaseLogic.pointsLogic.PointsRepository
import kotlinx.coroutines.launch

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.example.daypilot.main.mainZone.habits.steps.StepsForegroundSync
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.ZoneId


class MainActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var sessionManager: SessionManager
    private val techMonitor by lazy { com.example.daypilot.main.mainZone.habits.tech.TechHealthMonitor(applicationContext) }
    private lateinit var stepsSync: StepsForegroundSync

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepsSync = StepsForegroundSync(
            appContext = applicationContext,
            zoneId = ZoneId.systemDefault()
        )

        WindowCompat.setDecorFitsSystemWindows(window, true)
        sessionManager = SessionManager(this)


        if (!com.example.daypilot.mainDatabase.SessionGuard.ensureValidSessionOrLogout(
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
            goToLoginAndFinish()
            return
        }

        lifecycleScope.launch {
            runCatching {
                PointsRepository().refreshAndPurge(user.uid)
                try { com.example.daypilot.firebaseLogic.AppOpenStatsSync.run(user.uid) } catch (_: Exception) {}
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    runCatching { techMonitor.tick() }
                    delay(60_000L)
                }
            }
        }

            setContent {
                val darkTheme = sessionManager.isDarkModeEnabled()
                val colorScheme = MaterialTheme.colorScheme
                DayPilotTheme (darkTheme = darkTheme){

                    SideEffect {
                        window.statusBarColor = colorScheme.background.toArgb()
                        window.navigationBarColor = colorScheme.background.toArgb()

                        WindowInsetsControllerCompat(window, window.decorView).apply {
                            isAppearanceLightStatusBars = !darkTheme
                            isAppearanceLightNavigationBars = !darkTheme
                        }
                    }
                    MainScreen(
                        authRepo = authRepo,
                        sessionManager = sessionManager,
                        onLogoutToLogin = { goToLoginAndFinish() }
                    )
                }
            }
        }

    private fun goToLoginAndFinish() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    override fun onResume() {
        super.onResume()
        val uid = authRepo.currentUser?.uid ?: return
        lifecycleScope.launch {
            stepsSync.sync(uid)
        }
    }
}