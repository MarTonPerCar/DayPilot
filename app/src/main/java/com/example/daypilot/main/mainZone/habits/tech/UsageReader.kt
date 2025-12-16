package com.example.daypilot.main.mainZone.habits.tech

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Process

object UsageReader {

    fun hasUsageAccess(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }


    fun currentTopPackage(context: Context, lookbackMs: Long = 30_000L): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val end = System.currentTimeMillis()
        val start = (end - lookbackMs).coerceAtLeast(0L)

        val events = usm.queryEvents(start, end)
        val e = UsageEvents.Event()

        var lastTop: String? = null
        var lastTopTs = 0L

        while (events.hasNextEvent()) {
            events.getNextEvent(e)

            val isForeground =
                e.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND ||
                        e.eventType == UsageEvents.Event.ACTIVITY_RESUMED

            if (isForeground) {
                lastTop = e.packageName
                lastTopTs = e.timeStamp
            }
        }

        return lastTop
    }
}