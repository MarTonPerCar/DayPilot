package com.example.daypilot_test_desing.data.supabase

import android.util.Log
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
import com.example.daypilot_test_desing.data.supabase.dto.CalendarTaskDto
import com.example.daypilot_test_desing.data.supabase.dto.NewTaskDayDto
import com.example.daypilot_test_desing.data.supabase.dto.NewTaskDto
import com.example.daypilot_test_desing.data.supabase.dto.TaskIdDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.Calendar
import java.util.UUID

class SupabaseTaskRepository : TaskRepository {

    companion object {
        private const val TAG = "SupabaseTaskRepository"
    }

    private fun userId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun getTasks(): List<CalendarTaskData> {
        SessionCache.tasks.value?.let { return it }
        val uid = userId() ?: return emptyList()
        return try {
            val today = Calendar.getInstance()
            val from = (today.clone() as Calendar).also { it.add(Calendar.DAY_OF_YEAR, -90) }
            val to = (today.clone() as Calendar).also { it.add(Calendar.DAY_OF_YEAR, 180) }
            val fromDate = "%04d-%02d-%02d".format(from.get(Calendar.YEAR), from.get(Calendar.MONTH) + 1, from.get(Calendar.DAY_OF_MONTH))
            val toDate = "%04d-%02d-%02d".format(to.get(Calendar.YEAR), to.get(Calendar.MONTH) + 1, to.get(Calendar.DAY_OF_MONTH))

            val result = supabase.from("calendar_tasks")
                .select {
                    filter {
                        eq("user_id", uid)
                        gte("date", fromDate)
                        lte("date", toDate)
                    }
                }
                .decodeList<CalendarTaskDto>()
                .map { it.toModel() }
            SessionCache.tasks.value = result
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load tasks", e)
            emptyList()
        }
    }

    override suspend fun addTask(data: NewTaskData) {
        val uid = userId() ?: return
        val taskId = UUID.randomUUID().toString()
        val date = "%04d-%02d-%02d".format(data.year, data.month, data.day)

        try {
            // Read back the id Postgrest actually stored rather than trusting the
            // client-generated one — they can diverge (server-side default/trigger),
            // and task_days' FK insert must reference the real stored id or it 23503s.
            val realTaskId = supabase.from("tasks").insert(buildJsonObject {
                put("id",                taskId)
                put("user_id",           uid)
                put("title",             data.title)
                put("description",       data.description.ifBlank { null })
                put("category",          data.category.toDbString())
                put("difficulty",        data.difficulty.name)
                put("estimated_minutes", data.duration)
                put("reminder_enabled",  data.hasReminder)
                put("is_recurring",      data.isRecurring)
            }) {
                select(Columns.raw("id"))
            }.decodeSingle<TaskIdDto>().id
            Log.d(TAG, "Inserted task $realTaskId '${data.title}' (client id was $taskId)")

            supabase.from("task_days").insert(
                NewTaskDayDto(taskId = realTaskId, userId = uid, date = date)
            )
            Log.d(TAG, "Inserted first task_days row for $realTaskId on $date")

            if (data.isRecurring && data.recurrenceDays >= 1) {
                val cal = Calendar.getInstance()
                cal.set(data.year, data.month - 1, data.day)
                cal.add(Calendar.DAY_OF_YEAR, data.recurrenceDays)

                val limit = Calendar.getInstance()
                limit.set(data.year, data.month - 1, data.day)
                limit.add(Calendar.DAY_OF_YEAR, 90)

                val recurringDays = mutableListOf<NewTaskDayDto>()
                while (!cal.after(limit)) {
                    val recurDate = "%04d-%02d-%02d".format(
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH) + 1,
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                    recurringDays += NewTaskDayDto(taskId = realTaskId, userId = uid, date = recurDate)
                    cal.add(Calendar.DAY_OF_YEAR, data.recurrenceDays)
                }
                if (recurringDays.isNotEmpty()) {
                    supabase.from("task_days").insert(recurringDays)
                    Log.d(TAG, "Inserted ${recurringDays.size} recurring task_days rows for $realTaskId")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert task '${data.title}' (id=$taskId, recurring=${data.isRecurring})", e)
            throw e
        }

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
        try {
            supabase.from("tasks").update({
                set("title", title)
                set("description", description.ifBlank { null })
                set("category", category.toDbString())
                set("difficulty", difficulty.name)
                set("estimated_minutes", duration)
            }) {
                filter { eq("id", id); eq("user_id", uid) }
            }
            Log.d(TAG, "Updated task $id")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update task $id", e)
            throw e
        }
        SessionCache.tasks.value = null
    }

    override suspend fun toggleTask(occurrenceId: String, isDone: Boolean) {
        val uid = userId() ?: return
        val completedAt: String? = if (isDone) java.time.Instant.now().toString() else null
        try {
            supabase.from("task_days").update({
                set("is_completed", isDone)
                set("completed_at", completedAt)
                // is_earned only ever goes false -> true; never reset on uncheck, so a
                // task can't be paid out twice by unchecking and rechecking it.
                if (isDone) set("is_earned", true)
            }) {
                filter {
                    eq("id", occurrenceId)
                    eq("user_id", uid)
                }
            }
            Log.d(TAG, "Toggled task occurrence $occurrenceId to isDone=$isDone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle task occurrence $occurrenceId to isDone=$isDone", e)
            throw e
        }
        SessionCache.tasks.value = null
    }

    override suspend fun deleteTask(id: String) {
        val uid = userId() ?: return
        try {
            supabase.from("tasks").delete {
                filter { eq("id", id); eq("user_id", uid) }
            }
            Log.d(TAG, "Deleted task $id")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete task $id", e)
            throw e
        }
        SessionCache.tasks.value = null
    }
}

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
        isEarned     = isEarned,
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
