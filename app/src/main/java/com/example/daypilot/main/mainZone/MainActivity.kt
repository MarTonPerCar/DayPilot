// ==================================================
// MainActivity.kt
// ==================================================
package com.example.daypilot.main.mainZone

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.daypilot.firebaseLogic.AppOpenStatsSync
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.pointsLogic.PointsRepository
import com.example.daypilot.login.LoginActivity
import com.example.daypilot.mainDatabase.SessionGuard
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.main.mainZone.habits.steps.StepsForegroundSync
import com.example.daypilot.main.mainZone.habits.tech.TechHealthMonitor
import com.example.daypilot.ui.theme.DayPilotTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.ZoneId

class MainActivity : ComponentActivity() {

    // ========== Dependencies ==========

    private val authRepo = AuthRepository()
    private val pointsRepo = PointsRepository()
    private lateinit var sessionManager: SessionManager
    private val techMonitor by lazy { TechHealthMonitor(applicationContext) }

    private val stepsSync by lazy {
        StepsForegroundSync(
            appContext = applicationContext,
            zoneId = ZoneId.systemDefault()
        )
    }

    // ========== Lifecycle ==========

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        sessionManager = SessionManager(this)

        if (!ensureValidSession()) return

        val uid = authRepo.currentUser?.uid ?: run {
            goToLoginAndFinish()
            return
        }

        launchStartupWork(uid)
        launchTechMonitorLoop()

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

                MainScreen(
                    authRepo = authRepo
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val uid = authRepo.currentUser?.uid ?: return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        lifecycleScope.launch {
            runCatching { stepsSync.sync(uid) }
        }
    }

    // ========== Private ==========

    private fun ensureValidSession(): Boolean {
        val ok = SessionGuard.ensureValidSessionOrLogout(
            context = this,
            sessionManager = sessionManager,
            authRepo = authRepo
        )
        if (!ok) {
            finish()
            return false
        }
        return true
    }

    private fun launchStartupWork(uid: String) {
        lifecycleScope.launch {
            runCatching { pointsRepo.refreshAndPurge(uid) }
            runCatching { AppOpenStatsSync.run(uid) }
        }
    }

    private fun launchTechMonitorLoop() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                while (isActive) {
                    runCatching { techMonitor.tick() }
                    delay(60_000L)
                }
            }
        }
    }

    private fun goToLoginAndFinish() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }
}