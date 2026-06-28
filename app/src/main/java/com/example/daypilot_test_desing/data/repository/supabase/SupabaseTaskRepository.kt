package com.example.daypilot_test_desing.data.repository.supabase

import com.example.daypilot_test_desing.data.model.CalendarTaskData
import com.example.daypilot_test_desing.data.model.NewTaskData
import com.example.daypilot_test_desing.data.model.TaskCategory
import com.example.daypilot_test_desing.data.model.TaskDifficulty
import com.example.daypilot_test_desing.data.repository.TaskRepository
import com.example.daypilot_test_desing.data.repository.supabase.dto.CalendarTaskDto
import com.example.daypilot_test_desing.data.repository.supabase.dto.NewTaskDayDto
import com.example.daypilot_test_desing.data.repository.supabase.dto.NewTaskDto
import com.example.daypilot_test_desing.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
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
                id = taskId,
                userId = uid,
                title = data.title,
                category = data.category.toDbString(),
                difficulty = data.difficulty.name,        // "EASY", "MEDIUM", "HARD"
                estimatedMinutes = data.duration
            )
        )
        supabase.from("task_days").insert(
            NewTaskDayDto(taskId = taskId, userId = uid, date = date)
        )
    }

    override suspend fun updateTask(
        id: String,
        title: String,
        category: TaskCategory,
        difficulty: TaskDifficulty,
        duration: Int
    ) {
        supabase.from("tasks").update({
            set("title", title)
            set("category", category.toDbString())
            set("difficulty", difficulty.name)            // "EASY", "MEDIUM", "HARD"
            set("estimated_minutes", duration)
        }) {
            filter { eq("id", id) }
        }
    }

    // is_completed lives on `tasks`, not task_days
    override suspend fun toggleTask(id: String, isDone: Boolean) {
        supabase.from("tasks").update({
            set("is_completed", isDone)
        }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun deleteTask(id: String) {
        supabase.from("tasks").delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun editTask(id: String) { /* edit is handled locally in the UI */ }
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
        id         = id,
        day        = parts[2].toInt(),
        month      = parts[1].toInt(),
        year       = parts[0].toInt(),
        title      = title,
        category   = category.toTaskCategory(),
        difficulty = difficulty.toTaskDifficulty(),
        duration   = estimatedMinutes,
        isDone     = isCompleted
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
