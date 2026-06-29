package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.model.CalendarTaskData
import com.example.daypilot_test_desing.backend.model.NewTaskData
import com.example.daypilot_test_desing.backend.model.TaskCategory
import com.example.daypilot_test_desing.backend.model.TaskDifficulty
import com.example.daypilot_test_desing.backend.repository.TaskRepository
import com.example.daypilot_test_desing.backend.supabase.dto.CalendarTaskDto
import com.example.daypilot_test_desing.backend.supabase.dto.NewTaskDayDto
import com.example.daypilot_test_desing.backend.supabase.dto.NewTaskDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.Calendar
import java.util.UUID

class SupabaseTaskRepository : TaskRepository {

    private fun userId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun getTasks(): List<CalendarTaskData> {
        val uid = userId() ?: return emptyList()
        return try {
            supabase.from("calendar_tasks")
                .select { filter { eq("user_id", uid) } }
                .decodeList<CalendarTaskDto>()
                .map { it.toModel() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun addTask(data: NewTaskData) {
        val uid = userId() ?: return
        val taskId = UUID.randomUUID().toString()
        val date = "%04d-%02d-%02d".format(data.year, data.month, data.day)

        supabase.from("tasks").insert(
            NewTaskDto(
                id               = taskId,
                userId           = uid,
                title            = data.title,
                description      = data.description.ifBlank { null },
                category         = data.category.toDbString(),
                difficulty       = data.difficulty.name,
                estimatedMinutes = data.duration,
                reminderEnabled  = data.hasReminder,
                isRecurring      = data.isRecurring
            )
        )
        supabase.from("task_days").insert(
            NewTaskDayDto(taskId = taskId, userId = uid, date = date)
        )

        // For recurring tasks, generate day entries for the next 90 days
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
    }

    override suspend fun updateTask(
        id: String,
        title: String,
        category: TaskCategory,
        difficulty: TaskDifficulty,
        duration: Int,
        description: String
    ) {
        supabase.from("tasks").update({
            set("title", title)
            set("description", description.ifBlank { null })
            set("category", category.toDbString())
            set("difficulty", difficulty.name)
            set("estimated_minutes", duration)
        }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun toggleTask(id: String, isDone: Boolean) {
        val uid = userId() ?: return
        val completedAt: String? = if (isDone) java.time.Instant.now().toString() else null
        supabase.from("tasks").update({
            set("is_completed", isDone)
            set("completed_at", completedAt)
        }) {
            filter {
                eq("id", id)
                eq("user_id", uid)
            }
        }
    }

    override suspend fun deleteTask(id: String) {
        supabase.from("tasks").delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun editTask(id: String) { /* handled locally in UI */ }
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
        id          = id,
        day         = parts[2].toInt(),
        month       = parts[1].toInt(),
        year        = parts[0].toInt(),
        title       = title,
        category    = category.toTaskCategory(),
        difficulty  = difficulty.toTaskDifficulty(),
        duration    = estimatedMinutes,
        isDone      = isCompleted,
        description = description,
        isRecurring = isRecurring,
        hasReminder = reminderEnabled
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
