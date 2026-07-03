package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
import com.example.daypilot_test_desing.data.supabase.dto.CalendarTaskDto
import com.example.daypilot_test_desing.data.supabase.dto.NewTaskDayDto
import com.example.daypilot_test_desing.data.supabase.dto.NewTaskDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Calendar
import java.util.UUID

class SupabaseTaskRepository : TaskRepository {

    private fun userId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun getTasks(): List<CalendarTaskData> {
        SessionCache.tasks.value?.let { return it }
        val uid = userId() ?: return emptyList()
        return try {
            val result = supabase.from("calendar_tasks")
                .select { filter { eq("user_id", uid) } }
                .decodeList<CalendarTaskDto>()
                .map { it.toModel() }
            SessionCache.tasks.value = result
            result
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addTask(data: NewTaskData) {
        val uid = userId() ?: return
        val taskId = UUID.randomUUID().toString()
        val date = "%04d-%02d-%02d".format(data.year, data.month, data.day)

        val endCal = Calendar.getInstance().also {
            it.set(data.year, data.month - 1, data.day)
            it.add(Calendar.DAY_OF_YEAR, 90)
        }
        val recurrenceEndDate = "%04d-%02d-%02d".format(
            endCal.get(Calendar.YEAR),
            endCal.get(Calendar.MONTH) + 1,
            endCal.get(Calendar.DAY_OF_MONTH)
        )

        supabase.from("tasks").insert(buildJsonObject {
            put("id",                taskId)
            put("user_id",           uid)
            put("title",             data.title)
            put("description",       data.description.ifBlank { null })
            put("category",          data.category.toDbString())
            put("difficulty",        data.difficulty.name)
            put("estimated_minutes", data.duration)
            put("reminder_enabled",  data.hasReminder)
            put("is_recurring",      data.isRecurring)
            if (data.isRecurring) {
                put("recurrence_days",     data.recurrenceDays)
                put("recurrence_end_date", recurrenceEndDate)
            }
        })

        supabase.from("task_days").insert(
            NewTaskDayDto(taskId = taskId, userId = uid, date = date)
        )

        // TODO: replace with a single batch insert or a DB function — currently one round-trip per day
        if (data.isRecurring && data.recurrenceDays >= 1) {
            val cal = Calendar.getInstance()
            cal.set(data.year, data.month - 1, data.day)
            cal.add(Calendar.DAY_OF_YEAR, data.recurrenceDays)

            val limit = Calendar.getInstance()
            limit.set(data.year, data.month - 1, data.day)
            limit.add(Calendar.DAY_OF_YEAR, 90)

            while (!cal.after(limit)) {
                val recurDate = "%04d-%02d-%02d".format(
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH) + 1,
                    cal.get(Calendar.DAY_OF_MONTH)
                )
                try {
                    supabase.from("task_days").insert(
                        NewTaskDayDto(taskId = taskId, userId = uid, date = recurDate)
                    )
                } catch (_: Exception) { }
                cal.add(Calendar.DAY_OF_YEAR, data.recurrenceDays)
            }
        }

        // Invalidate so the next getTasks() fetches the real server-side row
        SessionCache.tasks.value = null
    }

    override suspend fun updateTask(
        id: String,
        title: String,
        category: TaskCategory,
        difficulty: TaskDifficulty,
        duration: Int,
        description: String
    ) {
        val uid = userId() ?: return
        supabase.from("tasks").update({
            set("title", title)
            set("description", description.ifBlank { null })
            set("category", category.toDbString())
            set("difficulty", difficulty.name)
            set("estimated_minutes", duration)
        }) {
            filter { eq("id", id); eq("user_id", uid) }
        }
        SessionCache.tasks.value = null
    }

    override suspend fun toggleTask(occurrenceId: String, isDone: Boolean) {
        val uid = userId() ?: return
        val completedAt: String? = if (isDone) java.time.Instant.now().toString() else null
        supabase.from("task_days").update({
            set("is_completed", isDone)
            set("completed_at", completedAt)
        }) {
            filter {
                eq("id", occurrenceId)
                eq("user_id", uid)
            }
        }
        SessionCache.tasks.value = null
    }

    override suspend fun deleteTask(id: String) {
        val uid = userId() ?: return
        supabase.from("tasks").delete {
            filter { eq("id", id); eq("user_id", uid) }
        }
        SessionCache.tasks.value = null
    }
}

// ── DB ↔ App mappings ─────────────────────────────────────────────

private fun TaskCategory.toDbString(): String = when (this) {
    TaskCategory.WORK     -> "Trabajo"
    TaskCategory.STUDY    -> "Estudio"
    TaskCategory.SPORT    -> "Deporte"
    TaskCategory.HEALTH   -> "Salud"
    TaskCategory.PERSONAL -> "General"
    TaskCategory.HOME     -> "Hogar"
    TaskCategory.OTHER    -> "General"
}

private fun CalendarTaskDto.toModel(): CalendarTaskData {
    val parts = date.split("-")
    return CalendarTaskData(
        id           = taskId,
        occurrenceId = occurrenceId,
        day          = parts[2].toInt(),
        month        = parts[1].toInt(),
        year         = parts[0].toInt(),
        title        = title,
        category     = category.toTaskCategory(),
        difficulty   = difficulty.toTaskDifficulty(),
        duration     = estimatedMinutes,
        isDone       = isCompleted,
        description  = description,
        isRecurring  = isRecurring,
        hasReminder  = reminderEnabled
    )
}

private fun String.toTaskCategory(): TaskCategory = when (this) {
    "Estudio"  -> TaskCategory.STUDY
    "Trabajo"  -> TaskCategory.WORK
    "Deporte"  -> TaskCategory.SPORT
    "Salud"    -> TaskCategory.HEALTH
    "Bienestar"-> TaskCategory.HEALTH
    "General"  -> TaskCategory.OTHER
    "Hogar"    -> TaskCategory.HOME
    "Compra"   -> TaskCategory.HOME
    "Finanzas" -> TaskCategory.WORK
    else       -> TaskCategory.OTHER
}

private fun String.toTaskDifficulty(): TaskDifficulty = when (this.uppercase()) {
    "MEDIUM" -> TaskDifficulty.MEDIUM
    "HARD"   -> TaskDifficulty.HARD
    else     -> TaskDifficulty.EASY
}
