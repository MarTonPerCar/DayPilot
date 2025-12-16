package com.example.daypilot.main.mainZone.habits.tech

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppCatalog {

    fun loadLaunchableApps(context: Context): List<AppEntry> {
        val pm = context.packageManager

        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return activities.mapNotNull { ri ->
            val pkg = ri.activityInfo?.packageName ?: return@mapNotNull null
            val label = ri.loadLabel(pm)?.toString() ?: pkg
            val icon = runCatching { ri.loadIcon(pm) }.getOrNull()
            AppEntry(packageName = pkg, label = label, icon = icon)
        }.distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }
}