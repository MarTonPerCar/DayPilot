package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.data.model.TimeZoneRegion
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.support.SharedFakeSupabaseClient
import com.example.daypilot_test_desing.support.fakeLogin
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
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
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

@RunWith(RobolectricTestRunner::class)
class SupabaseUserRepositoryTest {

    companion object {
        private val shared = SharedFakeSupabaseClient()
    }

    private fun fakeSupabaseClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = shared.use(handler)

    @Before
    fun setUp() = runBlocking {
        initSupabaseSettingsForTest()
        shared.resetAuth()
        SessionCache.clear()
    }

    @Test
    fun `getCurrentUser returns the cached profile without hitting the network`() = runTest {
        val cached = UserProfile(id = "u1", name = "Cached", username = "cached", email = "c@x.com")
        SessionCache.userProfile.value = cached
        val repo = SupabaseUserRepository(fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(cached, repo.getCurrentUser())
    }

    @Test
    fun `getCurrentUser returns a blank profile when nobody is logged in`() = runTest {
        val repo = SupabaseUserRepository(fakeSupabaseClient { error("no HTTP expected") })

        val result = repo.getCurrentUser()

        assertEquals("", result.id)
    }

    @Test
    fun `getCurrentUser returns an id-only profile when the user row is missing`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        val result = repo.getCurrentUser()

        assertEquals("u1", result.id)
        assertEquals("", result.name)
    }

    @Test
    fun `getCurrentUser maps the full profile including streak, region and caches it`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/users") ->
                    respond(
                        """[{"id":"u1","name":"Ana","username":"ana","email":"a@x.com",
                            "region":"${TimeZoneRegion.EUROPE_MADRID.value}","created_at":"2024-03-01","level":3,
                            "total_points_historical":500,"points_to_next_level":40}]""",
                        headers = jsonHeaders
                    )
                request.url.encodedPath.endsWith("/user_streaks") ->
                    respond("""[{"user_id":"u1","current_streak":7,"longest_streak":20}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        val result = repo.getCurrentUser()

        assertEquals("Ana", result.name)
        assertEquals(TimeZoneRegion.EUROPE_MADRID, result.region)
        assertEquals("2024", result.memberSince)
        assertEquals(3, result.level)
        assertEquals(7, result.currentStreak)
        assertEquals(20, result.longestStreak)
        assertEquals(result, SessionCache.userProfile.value)
    }

    @Test
    fun `getCurrentUser falls back to the default region for an unknown value`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"id":"u1","name":"Ana","region":"not-a-real-region"}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        assertEquals(TimeZoneRegion.EUROPE_MADRID, repo.getCurrentUser().region)
    }

    @Test
    fun `getCurrentUser returns an id-only profile when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        assertEquals("u1", repo.getCurrentUser().id)
    }

    @Test
    fun `getWeeklySummary returns zeros when nobody is logged in`() = runTest {
        val repo = SupabaseUserRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(0, repo.getWeeklySummary().totalPoints)
    }

    @Test
    fun `getWeeklySummary returns zeros when there is no summary row yet`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        assertEquals(0, repo.getWeeklySummary().totalPoints)
    }

    @Test
    fun `getWeeklySummary attaches reactions with resolved sender names`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/user_weekly_summary") ->
                    respond(
                        """[{"id":"s1","user_id":"u1","week_start":"2026-07-20","total_points":100,
                            "total_tasks_completed":5,"total_steps":9000,"best_streak":6}]""",
                        headers = jsonHeaders
                    )
                request.url.encodedPath.endsWith("/reactions") ->
                    respond(
                        """[{"from_user_id":"f1","to_user_id":"u1","weekly_summary_id":"s1","type":"fire"}]""",
                        headers = jsonHeaders
                    )
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"id":"f1","name":"Friend"}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        val result = repo.getWeeklySummary()

        assertEquals(100, result.totalPoints)
        assertEquals(1, result.reactions.size)
        assertEquals("Friend", result.reactions.first().fromName)
        assertEquals(ReactionType.entries.first { it.name.lowercase() == "fire" }, result.reactions.first().reaction)
    }

    @Test
    fun `getWeeklySummary drops reactions whose sender or type can't be resolved`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/user_weekly_summary") ->
                    respond(
                        """[{"id":"s1","user_id":"u1","week_start":"2026-07-20"}]""",
                        headers = jsonHeaders
                    )
                request.url.encodedPath.endsWith("/reactions") ->
                    respond(
                        """[{"from_user_id":"unknown","to_user_id":"u1","weekly_summary_id":"s1","type":"fire"},
                            {"from_user_id":"f1","to_user_id":"u1","weekly_summary_id":"s1","type":"not-a-type"}]""",
                        headers = jsonHeaders
                    )
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"id":"f1","name":"Friend"}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        assertEquals(0, repo.getWeeklySummary().reactions.size)
    }

    @Test
    fun `getWeeklySummary degrades to no reactions when the reactions query fails`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/user_weekly_summary") ->
                    respond("""[{"id":"s1","user_id":"u1","week_start":"2026-07-20","total_points":10}]""", headers = jsonHeaders)
                else -> respondError(HttpStatusCode.InternalServerError)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        val result = repo.getWeeklySummary()
        assertEquals(10, result.totalPoints)
        assertEquals(0, result.reactions.size)
    }

    @Test
    fun `getWeeklySummary returns zeros when the summary query fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        assertEquals(0, repo.getWeeklySummary().totalPoints)
    }

    @Test
    fun `updateProfile is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseUserRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.updateProfile("New name", "newuser", TimeZoneRegion.EUROPE_MADRID)
    }

    @Test
    fun `updateProfile sends lowercased username and updates the cached profile`() = runTest {
        SessionCache.userProfile.value = UserProfile(id = "u1", name = "Old", username = "old", email = "e@x.com")
        var captured: String? = null
        val client = fakeSupabaseClient { request ->
            captured = String(request.body.toByteArray())
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        repo.updateProfile("New Name", "NewUser", TimeZoneRegion.EUROPE_MADRID)

        assertTrue(captured!!.contains("\"username_lower\":\"newuser\""))
        assertEquals("New Name", SessionCache.userProfile.value?.name)
    }

    @Test
    fun `uploadAvatar returns null when nobody is logged in`() = runTest {
        val repo = SupabaseUserRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertNull(repo.uploadAvatar(ByteArray(0), "image/jpeg"))
    }

    @Test
    fun `uploadAvatar stores the file, updates photo_url and the cached profile`() = runTest {
        SessionCache.userProfile.value = UserProfile(id = "u1", name = "Ana", username = "ana", email = "a@x.com")
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.contains("/object/avatars/") ->
                    respond("""{"Id":"file-id","Key":"avatars/u1/avatar.jpg"}""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") -> respond("[]", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        val url = repo.uploadAvatar(byteArrayOf(1, 2, 3), "image/jpeg")

        assertTrue(url != null)
        assertEquals(url, SessionCache.userProfile.value?.avatarUrl)
    }

    @Test
    fun `uploadAvatar returns null when the upload fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseUserRepository(client)

        assertNull(repo.uploadAvatar(byteArrayOf(1), "image/png"))
    }
}
