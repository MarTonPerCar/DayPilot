package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.support.SharedFakeSupabaseClient
import com.example.daypilot_test_desing.support.fakeLogin
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")
private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

class SupabaseProgressRepositoryTest {

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

    @Test
    fun `getTodayProgress returns the cached value when it's already for today`() = runTest {
        val cached = DailyProgressDto(userId = "u1", date = today(), steps = 500)
        SessionCache.todayProgress.value = cached
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(cached, repo.getTodayProgress())
    }

    @Test
    fun `getTodayProgress refetches when the cached value is for a different day`() = runTest {
        SessionCache.todayProgress.value = DailyProgressDto(userId = "u1", date = "2000-01-01")
        val client = fakeSupabaseClient {
            respond("""[{"user_id":"u1","date":"${today()}","steps":900}]""", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        val result = repo.getTodayProgress()

        assertEquals(900, result.steps)
        assertEquals(result, SessionCache.todayProgress.value)
    }

    @Test
    fun `getTodayProgress returns a blank-user placeholder when nobody is logged in`() = runTest {
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })

        val result = repo.getTodayProgress()

        assertEquals("", result.userId)
        assertEquals(today(), result.date)
    }

    @Test
    fun `getTodayProgress falls back to a fresh row when none exists yet`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        val result = repo.getTodayProgress()

        assertEquals("u1", result.userId)
        assertEquals(0, result.steps)
    }

    @Test
    fun `getTodayProgress returns a placeholder when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        val result = repo.getTodayProgress()

        assertEquals("", result.userId)
    }

    @Test
    fun `getHistory returns the cached value within the TTL`() = runTest {
        val cached = listOf(com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto(userId = "u1", date = today(), steps = 1, tasksCompleted = 1, totalPoints = 1))
        SessionCache.weeklyHistory.value = cached
        SessionCache.weeklyHistoryFetchedAt = System.currentTimeMillis()
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(cached, repo.getHistory(7))
    }

    @Test
    fun `getHistory returns empty when nobody is logged in`() = runTest {
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<Any>(), repo.getHistory(7))
    }

    @Test
    fun `getHistory fetches and caches recent logs`() = runTest {
        val client = fakeSupabaseClient {
            respond(
                """[{"user_id":"u1","date":"d1","steps":100,"tasks_completed":1,"total_points":10}]""",
                headers = jsonHeaders
            )
        }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        val result = repo.getHistory(7)

        assertEquals(1, result.size)
        assertEquals(result, SessionCache.weeklyHistory.value)
        assertTrue(SessionCache.weeklyHistoryFetchedAt > 0)
    }

    @Test
    fun `getHistory returns empty when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        assertEquals(emptyList<Any>(), repo.getHistory(7))
    }

    @Test
    fun `logPoints is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.logPoints(10, "TASKS")
    }

    @Test
    fun `logPoints inserts the row and updates cached today-progress and profile`() = runTest {
        SessionCache.todayProgress.value = DailyProgressDto(userId = "u1", date = today(), tasksPoints = 5, totalPoints = 5)
        SessionCache.userProfile.value = UserProfile(id = "u1", name = "N", username = "n", email = "e@x.com", totalPoints = 100, level = 3)
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        repo.logPoints(10, "TASKS")

        assertEquals(15, SessionCache.todayProgress.value?.tasksPoints)
        assertEquals(15, SessionCache.todayProgress.value?.totalPoints)
        assertEquals(110, SessionCache.userProfile.value?.totalPoints)
    }

    @Test
    fun `logPoints leaves cached progress and profile untouched when nothing was cached yet`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        repo.logPoints(10, "STEPS")

        assertEquals(null, SessionCache.todayProgress.value)
        assertEquals(null, SessionCache.userProfile.value)
    }

    @Test
    fun `getRankingPosition returns 0 when nobody is logged in`() = runTest {
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(0, repo.getRankingPosition())
    }

    @Test
    fun `getRankingPosition uses the cached ranking when present`() = runTest {
        SessionCache.ranking.value = listOf(
            com.example.daypilot_test_desing.core.data.model.RankingData(id = "f1", name = "F1", points = 100, streak = 0, level = 1),
            com.example.daypilot_test_desing.core.data.model.RankingData(id = "u1", name = "Me", points = 50, streak = 0, level = 1)
        )
        val client = fakeSupabaseClient { error("no HTTP expected") }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        assertEquals(2, repo.getRankingPosition())
    }

    @Test
    fun `getRankingPosition computes and caches the sorted ranking`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/friends") && request.url.encodedQuery.contains("requester_id=eq.") ->
                    respond("""[{"requester_id":"u1","receiver_id":"f1"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/friends") ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/friends_ranking") ->
                    respond(
                        """[{"id":"f1","name":"Friend","username":"friend","level":2,"points_last_30_days":200},
                            {"id":"u1","name":"Me","username":"me","level":1,"points_last_30_days":100}]""",
                        headers = jsonHeaders
                    )
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        val position = repo.getRankingPosition()

        assertEquals(2, position)
        assertEquals(2, SessionCache.ranking.value?.size)
        assertTrue(SessionCache.rankingFetchedAt > 0)
    }

    @Test
    fun `getRankingPosition returns 0 when the ranking request fails`() = runTest {
        val client = fakeSupabaseClient { request ->
            if (request.url.encodedPath.endsWith("/friends_ranking")) {
                respondError(HttpStatusCode.InternalServerError)
            } else {
                respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        assertEquals(0, repo.getRankingPosition())
    }

    @Test
    fun `completeTimerSession returns false when nobody is logged in`() = runTest {
        val repo = SupabaseProgressRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(false, repo.completeTimerSession())
    }

    @Test
    fun `completeTimerSession returns false when the point was already earned today`() = runTest {
        val client = fakeSupabaseClient { respond("""[{"timer_point_earned":true}]""", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        assertEquals(false, repo.completeTimerSession())
    }

    @Test
    fun `completeTimerSession logs points and marks the day as earned`() = runTest {
        var upsertCaptured = false
        val client = fakeSupabaseClient { request ->
            when (request.method.value) {
                "GET" -> respond("[]", headers = jsonHeaders)
                else -> {
                    upsertCaptured = true
                    respond("[]", headers = jsonHeaders)
                }
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseProgressRepository(client)

        val result = repo.completeTimerSession()

        assertEquals(true, result)
        assertTrue(upsertCaptured)
    }
}
