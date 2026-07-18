package com.example.daypilot_test_desing.core.reminders

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

/**
 * TASK_REMINDER / STREAK_RISK notifications now arrive from Supabase cron jobs with
 * encoded title/body placeholders ("TASK_REMINDER_COUNT:3", "STREAK_RISK_BODY", ...)
 * instead of final display text. Decoding lives here so both the OS notification
 * (DailyNotificationsReceiver, which knows the type it queried for) and the in-app
 * notifications list (NotificationsScreen, which only has the raw title/body left
 * over from the DB row) resolve to the same localized strings.
 */
object NotificationBodyCodec {

    @StringRes
    fun titleForType(dbType: String): Int? = when (dbType) {
        "TASK_REMINDER" -> R.string.notif_task_reminder_title
        "STREAK_RISK"   -> R.string.notif_streak_danger_title
        else            -> null
    }

    @StringRes
    fun titleForPlaceholder(rawTitle: String): Int? = when (rawTitle) {
        "TASK_REMINDER_TITLE" -> R.string.notif_task_reminder_title
        "STREAK_RISK_TITLE"   -> R.string.notif_streak_danger_title
        else                  -> null
    }

    /** Resolved string resource id, plus an optional %1$d arg to format it with. */
    fun decodeBody(rawBody: String): Pair<Int, Int?>? = when {
        rawBody == "TASK_REMINDER_NONE" ->
            R.string.notif_task_reminder_none to null
        rawBody.startsWith("TASK_REMINDER_COUNT:") ->
            R.string.notif_task_reminder_count to (rawBody.substringAfter(":").toIntOrNull() ?: 0)
        rawBody == "STREAK_RISK_BODY" ->
            R.string.notif_streak_danger_body to null
        else -> null
    }
}
