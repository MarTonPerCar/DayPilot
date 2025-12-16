package com.example.daypilot.main.mainZone.habits.tech

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TechHealthBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != "android.intent.action.BOOT_COMPLETED") return
        TechHealthForegroundService.start(context.applicationContext, tickMs = 5_000L)
    }
}
