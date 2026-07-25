package com.example.daypilot_test_desing.core.reminders

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

/** A decoded notification body: either a plain string resource, or a plurals resource + quantity. */
sealed class NotificationBody {
    data class PlainText(@StringRes val resId: Int, val arg: Int? = null) : NotificationBody()
    data class Count(@PluralsRes val resId: Int, val quantity: Int) : NotificationBody()
}

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

    fun decodeBody(rawBody: String): NotificationBody? = when {
        rawBody == "TASK_REMINDER_NONE" ->
            NotificationBody.PlainText(R.string.notif_task_reminder_none)
        rawBody.startsWith("TASK_REMINDER_COUNT:") ->
            NotificationBody.Count(R.plurals.notif_task_reminder_count, rawBody.substringAfter(":").toIntOrNull() ?: 0)
        rawBody == "STREAK_RISK_BODY" ->
            NotificationBody.PlainText(R.string.notif_streak_danger_body)
        else -> null
    }
}
