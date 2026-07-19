package com.example.daypilot_test_desing.core.reminders

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.example.daypilot_test_desing.core.data.local.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import com.example.daypilot_test_desing.feature.techhealth.TechHealthBlockActivity
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class DayPilotAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "DayPilotAccessibility"

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

        if (pkg == packageName || pkg == "android" || pkg.startsWith("com.android.systemui")) return

        val now = System.currentTimeMillis()
        if (pkg == lastBlockedPkg && now - lastBlockMs < 2_500L) return

        checkAndBlock(pkg)
    }

    private fun checkAndBlock(pkg: String) {
        try {
            val usageMap = AppUsageTracker.getTodayUsage(this)

            val appRestriction = repo.getAppRestrictions().find { it.packageName == pkg && it.isEnabled }
            if (appRestriction != null) {
                val usedMinutes = usageMap[pkg] ?: appRestriction.usedMinutesToday
                if (usedMinutes >= appRestriction.dailyLimitMinutes) {
                    block(pkg, appRestriction.appName)
                    if (!appRestriction.isViolatedToday) {
                        repo.markViolated(appRestriction.id)
                        scope.launch { markAppViolatedInSupabase(pkg) }
                    }
                }
                return
            }

            val group = repo.getGroupRestrictions().find { g -> g.isEnabled && g.apps.any { it.packageName == pkg } }
            if (group != null) {
                val usedMinutes = group.apps.sumOf { usageMap[it.packageName] ?: 0 }
                if (usedMinutes >= group.dailyLimitMinutes) {
                    block(pkg, group.groupName)
                    if (!group.isViolatedToday) {
                        repo.markGroupViolated(group.id)
                        scope.launch { markGroupViolatedInSupabase(group.groupName) }
                    }
                }
            }
        } catch (e: Exception) {
            // Must not crash — a crashed accessibility service silently disables all TechHealth blocking.
            Log.e(TAG, "checkAndBlock failed for $pkg", e)
        }
    }

    private fun block(pkg: String, label: String) {
        lastBlockedPkg = pkg
        lastBlockMs    = System.currentTimeMillis()
        startActivity(
            Intent(this, TechHealthBlockActivity::class.java).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
                putExtra(TechHealthBlockActivity.EXTRA_APP_NAME, label)
                putExtra(TechHealthBlockActivity.EXTRA_PACKAGE,  pkg)
            }
        )
    }

    private suspend fun markAppViolatedInSupabase(packageName: String) {
        try {
            val uid = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.from("tech_health_config").update({ set("is_violated_today", true) }) {
                filter { eq("user_id", uid); eq("app_package", packageName) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark app $packageName as violated", e)
        }
    }

    private suspend fun markGroupViolatedInSupabase(groupName: String) {
        try {
            val uid = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.from("tech_health_group_config").update({ set("is_violated_today", true) }) {
                filter { eq("user_id", uid); eq("group_name", groupName) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark group $groupName as violated", e)
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
