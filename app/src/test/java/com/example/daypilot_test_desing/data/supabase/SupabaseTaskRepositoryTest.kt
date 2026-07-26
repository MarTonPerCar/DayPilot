package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.support.SharedFakeSupabaseClient
import com.example.daypilot_test_desing.support.fakeLogin
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

class SupabaseTaskRepositoryTest {

    companion object {
        private val shared = SharedFakeSupabaseClient()
    }

    private fun fakeSupabaseClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = shared.use(handler)

    @Before
    fun setUp() = runBlocking {
        shared.resetAuth()
        SessionCache.clear()
    }

    private fun taskRow(category: String, difficulty: String) = """{
        "occurrence_id":"o1","task_id":"t1","user_id":"u1","title":"T",
        "category":"$category","difficulty":"$difficulty",
        "is_completed":false,"estimated_minutes":30,"date":"2026-07-15"
    }"""

    @Test
    fun `getTasks returns the cached list without hitting the network`() = runTest {
        val cached = listOf(com.example.daypilot_test_desing.core.data.model.CalendarTaskData(
            id = "t1", occurrenceId = "o1", day = 1, month = 1, year = 2026,
            title = "Cached", category = TaskCategory.WORK, difficulty = TaskDifficulty.EASY,
            duration = 10, isDone = false
        ))
        SessionCache.tasks.value = cached
        val repo = SupabaseTaskRepository(fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(cached, repo.getTasks())
    }

    @Test
    fun `getTasks returns empty when nobody is logged in`() = runTest {
        val repo = SupabaseTaskRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<Any>(), repo.getTasks())
    }

    @Test
    fun `getTasks maps every known category and difficulty label, and caches the result`() = runTest {
        val rows = listOf(
            "Estudio" to "MEDIUM", "Trabajo" to "HARD", "Deporte" to "EASY",
            "Salud" to "medium", "Bienestar" to "HARD", "General" to "EASY",
            "Hogar" to "MEDIUM", "Compra" to "HARD", "Finanzas" to "EASY",
            "SomethingElse" to "unknown"
        ).joinToString(",") { (cat, diff) -> taskRow(cat, diff) }
        val client = fakeSupabaseClient { respond("[$rows]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        val result = repo.getTasks()

        assertEquals(10, result.size)
        assertEquals(
            listOf(
                TaskCategory.STUDY, TaskCategory.WORK, TaskCategory.SPORT,
                TaskCategory.HEALTH, TaskCategory.HEALTH, TaskCategory.OTHER,
                TaskCategory.HOME, TaskCategory.HOME, TaskCategory.WORK,
                TaskCategory.OTHER
            ),
            result.map { it.category }
        )
        assertEquals(
            listOf(
                TaskDifficulty.MEDIUM, TaskDifficulty.HARD, TaskDifficulty.EASY,
                TaskDifficulty.MEDIUM, TaskDifficulty.HARD, TaskDifficulty.EASY,
                TaskDifficulty.MEDIUM, TaskDifficulty.HARD, TaskDifficulty.EASY,
                TaskDifficulty.EASY
            ),
            result.map { it.difficulty }
        )
        assertEquals(2026, result.first().year)
        assertEquals(7, result.first().month)
        assertEquals(15, result.first().day)
        assertEquals(result, SessionCache.tasks.value)
    }

    @Test
    fun `getTasks returns empty when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        assertEquals(emptyList<Any>(), repo.getTasks())
    }

    private fun newTask(isRecurring: Boolean = false, recurrenceDays: Int = 1) = NewTaskData(
        day = 15, month = 7, year = 2026,
        title = "New task", category = TaskCategory.WORK, difficulty = TaskDifficulty.EASY,
        duration = 30, isRecurring = isRecurring, recurrenceDays = recurrenceDays
    )

    @Test
    fun `addTask is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseTaskRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.addTask(newTask())
    }

    @Test
    fun `addTask inserts the task then a single task_days row for a non-recurring task`() = runTest {
        var taskDaysInsertCount = 0
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/tasks") ->
                    respond("""[{"id":"real-id"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/task_days") -> {
                    taskDaysInsertCount++
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.addTask(newTask())

        assertEquals(1, taskDaysInsertCount)
        assertEquals(null, SessionCache.tasks.value)
    }

    @Test
    fun `addTask invalidates a previously cached task list`() = runTest {
        SessionCache.tasks.value = emptyList()
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/tasks") -> respond("""[{"id":"real-id"}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.addTask(newTask())

        assertEquals(null, SessionCache.tasks.value)
    }

    @Test
    fun `addTask inserts recurring occurrences within the 90-day window`() = runTest {
        var bulkInsertSize = 0
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/tasks") ->
                    respond("""[{"id":"real-id"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/task_days") -> {
                    val body = String(request.body.toByteArray())
                    // The bulk recurrence insert sends a JSON array; the first single insert doesn't.
                    if (body.trimStart().startsWith("[")) {
                        bulkInsertSize = body.split("task_id").size - 1
                    }
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.addTask(newTask(isRecurring = true, recurrenceDays = 30))

        // Starting the day after the task's date, every 30 days, up to +90 days: 3 occurrences.
        assertEquals(3, bulkInsertSize)
    }

    @Test
    fun `addTask propagates the failure when the task insert fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        try {
            repo.addTask(newTask())
            fail("expected an exception to propagate")
        } catch (_: Exception) {
            // expected
        }
    }

    @Test
    fun `updateTask is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseTaskRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.updateTask("t1", "New title", TaskCategory.WORK, TaskDifficulty.HARD, 45, "desc")
    }

    @Test
    fun `updateTask sends the new fields and invalidates the cache`() = runTest {
        SessionCache.tasks.value = emptyList()
        var captured: String? = null
        val client = fakeSupabaseClient { request ->
            captured = String(request.body.toByteArray())
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.updateTask("t1", "New title", TaskCategory.STUDY, TaskDifficulty.HARD, 45, "desc")

        assertTrue(captured!!.contains("\"title\":\"New title\""))
        assertTrue(captured!!.contains("\"difficulty\":\"HARD\""))
        assertEquals(null, SessionCache.tasks.value)
    }

    @Test
    fun `updateTask propagates the failure`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        try {
            repo.updateTask("t1", "T", TaskCategory.WORK, TaskDifficulty.EASY, 10, "")
            fail("expected an exception to propagate")
        } catch (_: Exception) {
            // expected
        }
    }

    @Test
    fun `toggleTask is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseTaskRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.toggleTask("o1", true)
    }

    @Test
    fun `toggleTask marking done sets is_earned and a completed_at timestamp`() = runTest {
        var captured: String? = null
        val client = fakeSupabaseClient { request ->
            captured = String(request.body.toByteArray())
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.toggleTask("o1", true)

        assertTrue(captured!!.contains("\"is_completed\":true"))
        assertTrue(captured!!.contains("\"is_earned\":true"))
        assertTrue(captured!!.contains("\"completed_at\":\""))
    }

    @Test
    fun `toggleTask marking pending clears completed_at and omits is_earned`() = runTest {
        var captured: String? = null
        val client = fakeSupabaseClient { request ->
            captured = String(request.body.toByteArray())
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.toggleTask("o1", false)

        assertTrue(captured!!.contains("\"is_completed\":false"))
        assertTrue(captured!!.contains("\"completed_at\":null"))
        assertTrue(!captured!!.contains("is_earned"))
    }

    @Test
    fun `toggleTask propagates the failure`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        try {
            repo.toggleTask("o1", true)
            fail("expected an exception to propagate")
        } catch (_: Exception) {
            // expected
        }
    }

    @Test
    fun `deleteTask is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseTaskRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.deleteTask("t1")
    }

    @Test
    fun `deleteTask removes the row and invalidates the cache`() = runTest {
        SessionCache.tasks.value = emptyList()
        val client = fakeSupabaseClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/tasks"))
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        repo.deleteTask("t1")

        assertEquals(null, SessionCache.tasks.value)
    }

    @Test
    fun `deleteTask propagates the failure`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseTaskRepository(client)

        try {
            repo.deleteTask("t1")
            fail("expected an exception to propagate")
        } catch (_: Exception) {
            // expected
        }
    }
}
