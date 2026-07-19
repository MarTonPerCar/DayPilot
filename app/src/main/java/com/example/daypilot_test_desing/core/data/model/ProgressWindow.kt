package com.example.daypilot_test_desing.core.data.model

import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val WINDOW_SIZE = 30

// x-axis labels use the real calendar day, so they can roll over a month boundary (...25, 30, 5, 6).
fun buildProgressWindow(history: List<DailyLogDto>, today: DailyProgressDto): List<DayProgress> {
    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
    val byDate = history.associateBy { it.date }

    val result = mutableListOf<DayProgress>()
    for (offset in (WINDOW_SIZE - 1) downTo 1) {
        val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -offset) }
        val log = byDate[fmt.format(cal.time)]
        result += DayProgress(
            dayOfMonth     = cal.get(Calendar.DAY_OF_MONTH),
            points         = log?.totalPoints ?: 0,
            steps          = log?.steps ?: 0,
            tasksCompleted = log?.tasksCompleted ?: 0
        )
    }
    result += DayProgress(
        dayOfMonth     = Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
        points         = today.totalPoints,
        steps          = today.steps,
        tasksCompleted = today.tasksCompleted,
        isToday        = true
    )
    return result
}
