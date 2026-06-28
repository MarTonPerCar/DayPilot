package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.CalendarTaskData
import com.example.daypilot_test_desing.backend.model.NewTaskData
import com.example.daypilot_test_desing.backend.model.TaskCategory
import com.example.daypilot_test_desing.backend.model.TaskDifficulty
import com.example.daypilot_test_desing.backend.repository.TaskRepository
import java.util.Calendar
import java.util.UUID

object FakeTaskRepository : TaskRepository {

    private fun dateOffset(days: Int): Triple<Int, Int, Int> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, days)
        return Triple(
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.MONTH) + 1,
            cal.get(Calendar.YEAR)
        )
    }

    private val _tasks: MutableList<CalendarTaskData> by lazy {
        val (d0, m0, y0) = dateOffset(0)
        val (d1, m1, y1) = dateOffset(1)
        val (d2, m2, y2) = dateOffset(2)
        val (d3, m3, y3) = dateOffset(3)
        val (d4, m4, y4) = dateOffset(4)
        mutableListOf(
            CalendarTaskData("t1", d0, m0, y0, "Reunión de equipo",      TaskCategory.WORK,     TaskDifficulty.MEDIUM, 60,  true),
            CalendarTaskData("t2", d0, m0, y0, "Sesión de gimnasio",     TaskCategory.SPORT,    TaskDifficulty.HARD,   90,  true),
            CalendarTaskData("t3", d0, m0, y0, "Estudiar Kotlin",        TaskCategory.STUDY,    TaskDifficulty.MEDIUM, 120, true),
            CalendarTaskData("t4", d1, m1, y1, "Revisión del proyecto",  TaskCategory.WORK,     TaskDifficulty.HARD,   180, false),
            CalendarTaskData("t5", d1, m1, y1, "Meditación matutina",    TaskCategory.HEALTH,   TaskDifficulty.EASY,   20,  false),
            CalendarTaskData("t6", d2, m2, y2, "Llamada con familia",    TaskCategory.PERSONAL, TaskDifficulty.EASY,   30,  false),
            CalendarTaskData("t7", d3, m3, y3, "Limpiar el apartamento", TaskCategory.HOME,     TaskDifficulty.EASY,   45,  false),
            CalendarTaskData("t8", d4, m4, y4, "Lectura nocturna",       TaskCategory.PERSONAL, TaskDifficulty.EASY,   30,  false)
        )
    }

    override suspend fun getTasks(): List<CalendarTaskData> = _tasks.toList()

    override suspend fun addTask(data: NewTaskData) {
        _tasks.add(
            CalendarTaskData(
                id         = UUID.randomUUID().toString(),
                day        = data.day,
                month      = data.month,
                year       = data.year,
                title      = data.title,
                category   = data.category,
                difficulty = data.difficulty,
                duration   = data.duration,
                isDone     = false
            )
        )
    }

    override suspend fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int) {
        val idx = _tasks.indexOfFirst { it.id == id }
        if (idx >= 0) _tasks[idx] = _tasks[idx].copy(title = title, category = category, difficulty = difficulty, duration = duration)
    }

    override suspend fun toggleTask(id: String, isDone: Boolean) {
        val idx = _tasks.indexOfFirst { it.id == id }
        if (idx >= 0) _tasks[idx] = _tasks[idx].copy(isDone = isDone)
    }

    override suspend fun deleteTask(id: String) { _tasks.removeAll { it.id == id } }

    override suspend fun editTask(id: String) { /* no-op — edit is handled locally in the UI */ }
}
