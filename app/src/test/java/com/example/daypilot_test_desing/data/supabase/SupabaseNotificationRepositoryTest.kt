package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.data.model.NotificationType
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Instant

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

class SupabaseNotificationRepositoryTest {

    companion object {
        private val shared = SharedFakeSupabaseClient()
    }

    private fun fakeSupabaseClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = shared.use(handler)

    @Before
    fun setUp() = runBlocking {
        shared.resetAuth()
        SupabaseNotificationRepository.client = shared.client
    }

    @Test
    fun `getCurrentUserId reflects the client's auth state`() = runTest {
        assertNull(SupabaseNotificationRepository.getCurrentUserId())

        fakeSupabaseClient { error("no HTTP expected") }
        shared.client.fakeLogin("u1")

        assertEquals("u1", SupabaseNotificationRepository.getCurrentUserId())
    }

    @Test
    fun `getUnreadCount returns the row count`() = runTest {
        fakeSupabaseClient {
            respond(
                """[{"id":"n1"},{"id":"n2"},{"id":"n3"}]""",
                headers = jsonHeaders
            )
        }
        assertEquals(3, SupabaseNotificationRepository.getUnreadCount("u1"))
    }

    @Test
    fun `getUnreadCount returns 0 when the request fails`() = runTest {
        fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        assertEquals(0, SupabaseNotificationRepository.getUnreadCount("u1"))
    }

    @Test
    fun `getAll maps db rows to display models`() = runTest {
        val fiveMinAgo = Instant.now().minusSeconds(5 * 60).toString()
        fakeSupabaseClient {
            respond(
                """[{"id":"n1","user_id":"u1","type":"FRIEND_REQUEST","title":"T","body":"B","is_read":false,"created_at":"$fiveMinAgo"}]""",
                headers = jsonHeaders
            )
        }

        val result = SupabaseNotificationRepository.getAll("u1")

        assertEquals(1, result.size)
        val n = result.first()
        assertEquals("n1", n.id)
        assertEquals("B", n.message)
        assertEquals(NotificationType.SOCIAL, n.type)
        assertEquals("5 min", n.timeAgo)
    }

    @Test
    fun `getAll returns empty list when the request fails`() = runTest {
        fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        assertEquals(emptyList<Any>(), SupabaseNotificationRepository.getAll("u1"))
    }

    @Test
    fun `dbType mapping covers every known type plus an unknown fallback`() = runTest {
        val now = Instant.now().toString()
        fun row(type: String) = """{"id":"n","user_id":"u1","type":"$type","title":"t","body":"b","is_read":false,"created_at":"$now"}"""
        val types = listOf(
            "FRIEND_REQUEST" to NotificationType.SOCIAL,
            "FRIEND_ACCEPTED" to NotificationType.SOCIAL,
            "REACTION" to NotificationType.SOCIAL,
            "LEVEL_UP" to NotificationType.ACHIEVEMENT,
            "STREAK_RISK" to NotificationType.STREAK,
            "STEPS_GOAL" to NotificationType.STEPS,
            "TIMER_DONE" to NotificationType.ACHIEVEMENT,
            "TASK_COMPLETED" to NotificationType.TASK,
            "TASK_REMINDER" to NotificationType.REMINDER,
            "DAILY_SUMMARY" to NotificationType.REMINDER,
            "SOMETHING_UNKNOWN" to NotificationType.REMINDER
        )
        for ((dbType, expected) in types) {
            fakeSupabaseClient { respond("[${row(dbType)}]", headers = jsonHeaders) }
            val result = SupabaseNotificationRepository.getAll("u1")
            assertEquals("mapping for $dbType", expected, result.first().type)
        }
    }

    @Test
    fun `relativeTime buckets by minutes, hours, days and weeks`() = runTest {
        fun rowAt(instant: Instant) =
            """[{"id":"n","user_id":"u1","type":"TASK_REMINDER","title":"t","body":"b","is_read":false,"created_at":"$instant"}]"""

        fakeSupabaseClient { respond(rowAt(Instant.now().minusSeconds(30)), headers = jsonHeaders) }
        assertEquals("Ahora", SupabaseNotificationRepository.getAll("u1").first().timeAgo)

        fakeSupabaseClient { respond(rowAt(Instant.now().minusSeconds(3 * 3600)), headers = jsonHeaders) }
        assertEquals("3h", SupabaseNotificationRepository.getAll("u1").first().timeAgo)

        fakeSupabaseClient { respond(rowAt(Instant.now().minusSeconds(2L * 86400)), headers = jsonHeaders) }
        assertEquals("2d", SupabaseNotificationRepository.getAll("u1").first().timeAgo)

        fakeSupabaseClient { respond(rowAt(Instant.now().minusSeconds(14L * 86400)), headers = jsonHeaders) }
        assertEquals("2sem", SupabaseNotificationRepository.getAll("u1").first().timeAgo)
    }

    @Test
    fun `relativeTime returns blank for an unparseable timestamp`() = runTest {
        fakeSupabaseClient {
            respond(
                """[{"id":"n","user_id":"u1","type":"TASK_REMINDER","title":"t","body":"b","is_read":false,"created_at":"not-a-date"}]""",
                headers = jsonHeaders
            )
        }
        assertEquals("", SupabaseNotificationRepository.getAll("u1").first().timeAgo)
    }

    @Test
    fun `markAsRead updates the matching row`() = runTest {
        fakeSupabaseClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/notifications"))
            respond("[]", headers = jsonHeaders)
        }
        SupabaseNotificationRepository.markAsRead("n1")
    }

    @Test
    fun `markAllAsRead updates every unread row for the user`() = runTest {
        fakeSupabaseClient { request ->
            assertTrue(request.url.encodedQuery.contains("is_read=eq.false"))
            respond("[]", headers = jsonHeaders)
        }
        SupabaseNotificationRepository.markAllAsRead("u1")
    }

    @Test
    fun `insert stores the notification and prunes once past the retain limit`() = runTest {
        var insertCaptured: String? = null
        var deleteCaptured: String? = null
        fakeSupabaseClient { request ->
            when (request.method.value) {
                "POST" -> {
                    insertCaptured = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                "DELETE" -> {
                    deleteCaptured = request.url.encodedQuery
                    respond("[]", headers = jsonHeaders)
                }
                else -> {
                    // pruneOldest's select: return 51 rows so pruning kicks in.
                    val rows = (1..51).joinToString(",") { """{"id":"n$it"}""" }
                    respond("[$rows]", headers = jsonHeaders)
                }
            }
        }

        SupabaseNotificationRepository.insert("u1", "TASK_REMINDER", "Title", "Body")

        assertTrue(insertCaptured!!.contains("\"type\":\"TASK_REMINDER\""))
        assertTrue(deleteCaptured!!.contains("id=in."))
    }

    @Test
    fun `insert does not prune when at or below the retain limit`() = runTest {
        var deleteHappened = false
        fakeSupabaseClient { request ->
            when (request.method.value) {
                "POST" -> respond("[]", headers = jsonHeaders)
                "DELETE" -> {
                    deleteHappened = true
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[{\"id\":\"n1\"}]", headers = jsonHeaders)
            }
        }

        SupabaseNotificationRepository.insert("u1", "TASK_REMINDER", "Title", "Body")

        assertTrue(!deleteHappened)
    }

    @Test
    fun `getLatestOfTypeToday returns the newest matching row`() = runTest {
        fakeSupabaseClient {
            respond(
                """[{"id":"n1","user_id":"u1","type":"STREAK_RISK","title":"T","body":"B"}]""",
                headers = jsonHeaders
            )
        }

        val result = SupabaseNotificationRepository.getLatestOfTypeToday("u1", "STREAK_RISK")

        assertEquals("STREAK_RISK", result?.type)
        assertEquals("B", result?.rawBody)
    }

    @Test
    fun `getLatestOfTypeToday returns null when there is no match`() = runTest {
        fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        assertNull(SupabaseNotificationRepository.getLatestOfTypeToday("u1", "STREAK_RISK"))
    }

    @Test
    fun `getLatestOfTypeToday returns null when the request fails`() = runTest {
        fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        assertNull(SupabaseNotificationRepository.getLatestOfTypeToday("u1", "STREAK_RISK"))
    }

    @Test
    fun `insertForCurrentUser is a no-op when nobody is logged in`() = runTest {
        fakeSupabaseClient { error("no HTTP expected") }
        SupabaseNotificationRepository.insertForCurrentUser("TASK_REMINDER", "T", "B")
    }

    @Test
    fun `insertForCurrentUser inserts for the logged in user`() = runTest {
        var captured: String? = null
        fakeSupabaseClient { request ->
            when (request.method.value) {
                "POST" -> {
                    captured = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        shared.client.fakeLogin("u1")

        SupabaseNotificationRepository.insertForCurrentUser("TASK_REMINDER", "Title", "Body")

        assertTrue(captured!!.contains("\"user_id\":\"u1\""))
    }
}
