package com.example.daypilot_test_desing.core.reminders

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

// Shared by DailyNotificationsReceiver and NotificationsScreen so both decode encoded
// placeholders ("TASK_REMINDER_COUNT:3", ...) to the same localized strings.
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
