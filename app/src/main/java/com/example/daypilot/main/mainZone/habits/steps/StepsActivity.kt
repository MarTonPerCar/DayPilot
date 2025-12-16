package com.example.daypilot.main.mainZone.habits.steps

import android.Manifest
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
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.ui.theme.DayPilotTheme
import kotlinx.coroutines.launch
import java.time.ZoneId

class StepsActivity : ComponentActivity() {

    private val authRepo = AuthRepository()
    private lateinit var stepsSync: StepsForegroundSync

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        stepsSync = StepsForegroundSync(
            appContext = applicationContext,
            zoneId = ZoneId.systemDefault()
        )

        val sessionManager = SessionManager(this)
        val uid = authRepo.currentUser?.uid ?: run { finish(); return }

        val zoneId = ZoneId.systemDefault()
        val vm = androidx.lifecycle.ViewModelProvider(
            this,
            StepsViewModelFactory(applicationContext, uid, zoneId)
        )[StepsViewModel::class.java]

        setContent {
            val darkTheme = sessionManager.isDarkModeEnabled()
            val colorScheme = MaterialTheme.colorScheme

            DayPilotTheme(darkTheme = darkTheme) {

                SideEffect {
                    window.statusBarColor = colorScheme.background.toArgb()
                    window.navigationBarColor = colorScheme.background.toArgb()

                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = !darkTheme
                        isAppearanceLightNavigationBars = !darkTheme
                    }
                }

                val context = LocalContext.current
                var hasPermission by remember { mutableStateOf(Build.VERSION.SDK_INT < 29) }

                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted -> hasPermission = granted }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= 29) {
                        launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                    }
                }

                LaunchedEffect(hasPermission) {
                    if (hasPermission) vm.start()
                }

                StepsScreen(
                    hasPermission = hasPermission,
                    ui = vm.ui.collectAsState().value,
                    onBack = { finish() },
                    onChangeGoal = { vm.setGoal(it) },
                    onRequestPermission = { launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION) },
                    onConsumeUploadMessage = { vm.consumeUploadMessage() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val uid = authRepo.currentUser?.uid ?: return
        lifecycleScope.launch {
            stepsSync.sync(uid)
        }
    }
}