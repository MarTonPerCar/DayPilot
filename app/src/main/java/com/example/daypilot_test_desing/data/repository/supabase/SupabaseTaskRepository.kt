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
        return supabase.from("calendar_tasks")
            .select { filter { eq("user_id", uid) } }
            .decodeList<CalendarTaskDto>()
            .map { it.toModel() }
    }

    override suspend fun addTask(data: NewTaskData) {
        val uid = userId() ?: return
        val taskId = UUID.randomUUID().toString()
        val scheduledDate = "%04d-%02d-%02d".format(data.year, data.month, data.day)

        supabase.from("tasks").insert(
            NewTaskDto(
                id = taskId,
                userId = uid,
                title = data.title,
                category = data.category.name.lowercase(),
                difficulty = data.difficulty.name.lowercase(),
                durationMinutes = data.duration
            )
        )
        supabase.from("task_days").insert(
            NewTaskDayDto(
                taskId = taskId,
                userId = uid,
                scheduledDate = scheduledDate
            )
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
            set("category", category.name.lowercase())
            set("difficulty", difficulty.name.lowercase())
            set("duration_minutes", duration)
        }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun toggleTask(id: String, isDone: Boolean) {
        supabase.from("task_days").update({
            set("is_done", isDone)
        }) {
            filter { eq("task_id", id) }
        }
    }

    override suspend fun deleteTask(id: String) {
        supabase.from("tasks").delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun editTask(id: String) { /* edit is handled locally in the UI */ }
}

private fun CalendarTaskDto.toModel(): CalendarTaskData {
    val (year, month, day) = scheduledDate.split("-").map { it.toInt() }
    return CalendarTaskData(
        id = id,
        day = day,
        month = month,
        year = year,
        title = title,
        category = category.toTaskCategory(),
        difficulty = difficulty.toTaskDifficulty(),
        duration = durationMinutes,
        isDone = isDone
    )
}

private fun String.toTaskCategory(): TaskCategory = when (this.lowercase()) {
    "work"     -> TaskCategory.WORK
    "study"    -> TaskCategory.STUDY
    "sport"    -> TaskCategory.SPORT
    "health"   -> TaskCategory.HEALTH
    "personal" -> TaskCategory.PERSONAL
    "home"     -> TaskCategory.HOME
    else       -> TaskCategory.OTHER
}

private fun String.toTaskDifficulty(): TaskDifficulty = when (this.lowercase()) {
    "medium" -> TaskDifficulty.MEDIUM
    "hard"   -> TaskDifficulty.HARD
    else     -> TaskDifficulty.EASY
}
