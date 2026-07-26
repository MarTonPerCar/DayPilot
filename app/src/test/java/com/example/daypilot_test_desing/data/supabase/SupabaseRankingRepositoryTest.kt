package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.RankingData
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

class SupabaseRankingRepositoryTest {

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
    fun `getRanking returns the cached value within the TTL`() = runTest {
        val cached = listOf(RankingData(id = "f1", name = "F1", points = 1, streak = 0, level = 1))
        SessionCache.setRanking(cached)
        SessionCache.rankingFetchedAt = System.currentTimeMillis()
        val repo = SupabaseRankingRepository(fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(cached, repo.getRanking())
    }

    @Test
    fun `getRanking returns empty and does not cache when nobody is logged in`() = runTest {
        val repo = SupabaseRankingRepository(fakeSupabaseClient { error("no HTTP expected") })

        val result = repo.getRanking()

        assertEquals(emptyList<RankingData>(), result)
        assertNull(SessionCache.ranking.value)
    }

    @Test
    fun `getRanking builds a sorted ranking from friends and caches it`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/friends") && request.url.encodedQuery.contains("requester_id=eq.") ->
                    respond("""[{"requester_id":"u1","receiver_id":"f1"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/friends") -> respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/friends_ranking") ->
                    respond(
                        """[{"id":"u1","name":"","username":"me","level":1,"points_last_30_days":50},
                            {"id":"f1","name":"Friend","username":"friend","level":2,"points_last_30_days":150}]""",
                        headers = jsonHeaders
                    )
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        val result = repo.getRanking()

        assertEquals(2, result.size)
        assertEquals("f1", result.first().id)
        // Blank display name falls back to the username.
        assertEquals("me", result.last().name)
        assertEquals(result, SessionCache.ranking.value)
        assertTrue(SessionCache.rankingFetchedAt > 0)
    }

    @Test
    fun `getRanking returns empty when the ranking query fails`() = runTest {
        val client = fakeSupabaseClient { request ->
            if (request.url.encodedPath.endsWith("/friends_ranking")) {
                respondError(HttpStatusCode.InternalServerError)
            } else {
                respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        assertEquals(emptyList<RankingData>(), repo.getRanking())
    }

    @Test
    fun `getCurrentUserId returns blank when nobody is logged in`() = runTest {
        val repo = SupabaseRankingRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals("", repo.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserId returns the logged in user's id`() = runTest {
        val client = fakeSupabaseClient { error("no HTTP expected") }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        assertEquals("u1", repo.getCurrentUserId())
    }

    @Test
    fun `getCurrentUserData returns null when nobody is logged in`() = runTest {
        val repo = SupabaseRankingRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertNull(repo.getCurrentUserData())
    }

    @Test
    fun `getCurrentUserData returns null when the user row is missing`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        assertNull(repo.getCurrentUserData())
    }

    @Test
    fun `getCurrentUserData combines 30-day history with today's open total`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"id":"u1","name":"Me","level":2}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/user_streaks") ->
                    respond("""[{"user_id":"u1","current_streak":4}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/user_daily_log") ->
                    respond(
                        """[{"user_id":"u1","date":"d1","steps":0,"tasks_completed":0,"total_points":40},
                            {"user_id":"u1","date":"d2","steps":0,"tasks_completed":0,"total_points":60}]""",
                        headers = jsonHeaders
                    )
                request.url.encodedPath.endsWith("/daily_progress") ->
                    respond("""[{"user_id":"u1","date":"today","total_points":15}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        val result = repo.getCurrentUserData()

        assertEquals("u1", result?.id)
        assertEquals("Me", result?.name)
        assertEquals(4, result?.streak)
        assertEquals(2, result?.level)
        assertEquals(115, result?.points)
    }

    @Test
    fun `getCurrentUserData degrades to 0 when streak and points queries fail`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"id":"u1","name":"Me","level":1}]""", headers = jsonHeaders)
                else -> respondError(HttpStatusCode.InternalServerError)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        val result = repo.getCurrentUserData()

        assertEquals(0, result?.streak)
        assertEquals(0, result?.points)
    }

    @Test
    fun `getCurrentUserData returns null when the user query itself fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseRankingRepository(client)

        assertNull(repo.getCurrentUserData())
    }
}
