package com.example.daypilot_test_desing.core.reminders

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.view.accessibility.AccessibilityEvent
import com.example.daypilot_test_desing.core.data.local.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyTechDto
import com.example.daypilot_test_desing.data.supabase.supabase
import com.example.daypilot_test_desing.feature.techhealth.TechHealthBlockActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DayPilotAccessibilityService : AccessibilityService() {

    companion object {
        // Checks the system setting directly instead of relying on onServiceConnected(),
        // since that only fires while the service is alive and can't answer "is it granted?" on demand.
        fun isEnabled(context: Context): Boolean {
            val expected = ComponentName(context, DayPilotAccessibilityService::class.java)
            val enabledServices = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            ) ?: return false
            val splitter = TextUtils.SimpleStringSplitter(':').apply { setString(enabledServices) }
            for (component in splitter) {
                if (ComponentName.unflattenFromString(component) == expected) return true
            }
            return false
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // evitar que se relance la pantalla de bloqueo mil veces para la misma app
    @Volatile private var lastBlockedPkg = ""
    @Volatile private var lastBlockMs    = 0L

    private lateinit var repo: SharedPrefsTechHealthRepository

    override fun onServiceConnected() {
        repo = SharedPrefsTechHealthRepository(this)
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes          = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType        = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100L
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        // Skip our own app and system packages
        if (pkg == packageName || pkg == "android" || pkg.startsWith("com.android.systemui")) return

        val now = System.currentTimeMillis()
        if (pkg == lastBlockedPkg && now - lastBlockMs < 2_500L) return

        checkAndBlock(pkg)
    }

    private fun checkAndBlock(pkg: String) {
        // TODO: de momento solo comprueba apps individuales, los grupos no se gestionan aquí
        // pendingDelete apps stay enforced today — they're only removed tomorrow.
        val restriction = repo.getAppRestrictions().find {
            it.packageName == pkg && it.isEnabled
        } ?: return

        // cogemos datos frescos de UsageStats; si no hay permiso usamos el valor guardado
        //Log.d("TechHealth", "checking $pkg, used=${AppUsageTracker.getTodayUsage(this)[pkg]}")
        val usedMinutes = AppUsageTracker.getTodayUsage(this)[pkg] ?: restriction.usedMinutesToday

        if (usedMinutes < restriction.dailyLimitMinutes) return

        // Limit exceeded — apply debounce
        lastBlockedPkg = pkg
        lastBlockMs    = System.currentTimeMillis()

        // Mark violation locally and write to DB (only once per day)
        if (!repo.isViolatedToday()) {
            repo.markViolatedToday()
            scope.launch {
                try {
                    val uid = supabase.auth.currentUserOrNull()?.id ?: return@launch
                    supabase.from("habits_daily").upsert(
                        HabitsDailyTechDto(
                            userId                = uid,
                            date                  = today(),
                            techHealthPointEarned = false
                        )
                    )
                } catch (_: Exception) { }
            }
        }

        // Launch the block screen
        startActivity(
            Intent(this, TechHealthBlockActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra(TechHealthBlockActivity.EXTRA_APP_NAME, restriction.appName)
                putExtra(TechHealthBlockActivity.EXTRA_PACKAGE,  pkg)
            }
        )
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
