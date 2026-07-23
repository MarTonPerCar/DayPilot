package com.example.daypilot_test_desing.core.reminders

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings

/**
 * Exact alarms and battery-optimization exemption aren't runtime permissions — the user has to
 * flip them in system settings. Without them, daily task/streak alarms fall back to a lenient
 * setWindow() (deferrable under Doze), and background execution in general gets throttled —
 * this is what made notifications unreliable while the app was closed (steps now use a
 * foreground service instead, which is exempt from most of that throttling).
 */
object ReliabilitySettings {

    fun canScheduleExactAlarms(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(AlarmManager::class.java) ?: return true
        return am.canScheduleExactAlarms()
    }

    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        val pm = context.getSystemService(PowerManager::class.java) ?: return true
        return pm.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun exactAlarmSettingsIntent(context: Context) =
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))

    fun batteryOptimizationIntent(context: Context) =
        Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:${context.packageName}"))

    fun appDetailsSettingsIntent(context: Context) =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))

    /** Called once ever (guarded by AppPreferences), right after the regular runtime permission
     *  requests on first launch. */
    fun requestMissingOnce(context: Context) {
        if (!isIgnoringBatteryOptimizations(context)) {
            context.startActivity(batteryOptimizationIntent(context))
        }
        if (!canScheduleExactAlarms(context)) {
            context.startActivity(exactAlarmSettingsIntent(context))
        }
    }
}
