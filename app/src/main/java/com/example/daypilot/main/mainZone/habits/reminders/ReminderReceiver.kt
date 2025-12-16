package com.example.daypilot.main.mainZone.habits.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val id = intent.getStringExtra("id") ?: return@launch
                val kind = runCatching {
                    AlarmKind.valueOf(intent.getStringExtra("kind") ?: AlarmKind.MAIN.name)
                }.getOrDefault(AlarmKind.MAIN)

                val store = RemindersLocalStore(context)
                val reminder = store.getById(id) ?: return@launch
                if (!reminder.enabled) return@launch

                when (kind) {
                    AlarmKind.PRE -> {
                        ReminderNotifier.notifyPreNow(context, reminder)
                    }
                    AlarmKind.MAIN -> {
                        ReminderNotifier.notifyMainNow(context, reminder)

                        when (reminder.repeat) {
                            RepeatType.ONCE -> {
                                ReminderScheduler.cancel(context, reminder.id)
                                store.delete(reminder.id)
                            }
                            RepeatType.DAILY -> {
                                val h = reminder.hour ?: return@launch
                                val m = reminder.minute ?: return@launch
                                val next = nextDailyTriggerMillis(h, m)

                                val updated = reminder.copy(
                                    triggerAtMillis = next,
                                    lastPreSentForTriggerAt = 0L
                                )

                                store.update(updated)
                                ReminderScheduler.cancel(context, updated.id)
                                ReminderScheduler.scheduleSmart(context, store, updated)
                            }
                        }
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun nextDailyTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        if (cal.timeInMillis <= System.currentTimeMillis() + 1000) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}