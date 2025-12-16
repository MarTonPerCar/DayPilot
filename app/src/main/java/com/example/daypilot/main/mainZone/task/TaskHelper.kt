package com.example.daypilot.main.mainZone.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.daypilot.firebaseLogic.taskLogic.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

fun formatDisplayDate(isoDate: String): String {
    // isoDate = "yyyy-MM-dd"
    return try {
        val parts = isoDate.split("-")
        if (parts.size == 3) {
            val day = parts[2]
            val month = parts[1]
            "$day/$month"
        } else {
            isoDate
        }
    } catch (_: Exception) {
        isoDate
    }
}

fun formatDuration(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60

    return when {
        hours == 0 -> "$mins min"
        mins == 0 -> "${hours} h"
        else -> "${hours} h ${mins} min"
    }
}

fun categoryIconFor(category: String): ImageVector =
    when (category.lowercase(Locale.getDefault())) {
        "estudios" -> Icons.Default.School
        "trabajo" -> Icons.Default.Work
        "salud" -> Icons.Default.Favorite
        "personal" -> Icons.Default.Person
        "deporte" -> Icons.Default.Favorite
        "casa" -> Icons.Default.Home
        else -> Icons.Default.Home
    }

fun nextDateMillis(task: Task): Long? {
    val iso = task.days.minOrNull() ?: return null
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val d = sdf.parse(iso) ?: return null
        d.time
    } catch (_: Exception) {
        null
    }
}

fun daysBetweenToday(isoDate: String): Int? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        sdf.timeZone = TimeZone.getDefault()
        val targetDate = sdf.parse(isoDate) ?: return null

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val targetCal = Calendar.getInstance().apply {
            time = targetDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        ((targetCal.timeInMillis - todayCal.timeInMillis) / (1000L * 60L * 60L * 24L)).toInt()
    } catch (_: Exception) {
        null
    }
}
