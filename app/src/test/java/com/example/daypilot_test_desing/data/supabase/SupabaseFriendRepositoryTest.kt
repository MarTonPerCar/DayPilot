package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.ReactionType
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
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

@RunWith(RobolectricTestRunner::class)
class SupabaseFriendRepositoryTest {

    companion object {
        private val shared = SharedFakeSupabaseClient()
    }

    private fun fakeSupabaseClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = shared.use(handler)

    @Before
    fun setUp() = runBlocking {
        initSupabaseSettingsForTest()
        SessionCache.clear()
        shared.resetAuth()
    }

    @Test
    fun `getFriendIds returns empty when nobody is logged in`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<String>(), repo.getFriendIds())
    }

    @Test
    fun `getFriendIds merges requester-side and receiver-side rows, deduped`() = runTest {
        val client = fakeSupabaseClient { request ->
            val query = request.url.encodedQuery
            when {
                query.contains("requester_id=eq.") ->
                    respond("""[{"requester_id":"u1","receiver_id":"f1"}]""", headers = jsonHeaders)
                query.contains("receiver_id=eq.") ->
                    respond("""[{"requester_id":"f1","receiver_id":"u1"},{"requester_id":"f2","receiver_id":"u1"}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        val ids = repo.getFriendIds()

        assertEquals(setOf("f1", "f2"), ids.toSet())
        assertEquals(2, ids.size)
    }

    @Test
    fun `getFriends returns the cached list within the TTL without hitting the network`() = runTest {
        val cached = listOf(FriendData(id = "f1", name = "Cached", email = "c@x.com", points = 1, streak = 1))
        SessionCache.friends.value = cached
        SessionCache.friendsFetchedAt = System.currentTimeMillis()
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })

        assertEquals(cached, repo.getFriends())
    }

    @Test
    fun `getFriends returns empty when nobody is logged in`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<FriendData>(), repo.getFriends())
    }

    @Test
    fun `getFriends returns empty when the user has no friends`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        assertEquals(emptyList<FriendData>(), repo.getFriends())
    }

    @Test
    fun `getFriends assembles user, streak, weekly summary and reaction data, then caches it`() = runTest {
        val client = fakeSupabaseClient { request ->
            val path = request.url.encodedPath
            val query = request.url.encodedQuery
            when {
                path.endsWith("/friends") && query.contains("requester_id=eq.") ->
                    respond("""[{"requester_id":"u1","receiver_id":"f1"}]""", headers = jsonHeaders)
                path.endsWith("/friends") && query.contains("receiver_id=eq.") ->
                    respond("[]", headers = jsonHeaders)
                path.endsWith("/users") ->
                    respond(
                        """[{"id":"f1","name":"Friend One","email":"f1@x.com","total_points_historical":42}]""",
                        headers = jsonHeaders
                    )
                path.endsWith("/user_streaks") ->
                    respond("""[{"user_id":"f1","current_streak":5}]""", headers = jsonHeaders)
                path.endsWith("/user_weekly_summary") ->
                    respond(
                        """[{"id":"s1","user_id":"f1","week_start":"2026-07-20","total_steps":9000,"total_tasks_completed":3,"total_points":30,"best_streak":6}]""",
                        headers = jsonHeaders
                    )
                path.endsWith("/reactions") ->
                    respond(
                        """[{"from_user_id":"u1","to_user_id":"f1","weekly_summary_id":"s1","type":"fire"}]""",
                        headers = jsonHeaders
                    )
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        val friends = repo.getFriends()

        assertEquals(1, friends.size)
        val friend = friends.first()
        assertEquals("f1", friend.id)
        assertEquals("Friend One", friend.name)
        assertEquals(42, friend.points)
        assertEquals(5, friend.streak)
        assertEquals(30, friend.weeklySummary?.totalPoints)
        assertEquals(ReactionType.entries.first { it.name.lowercase() == "fire" }, friend.weeklySummary?.myReaction)

        // Result is cached for subsequent calls within the TTL.
        assertEquals(friends, SessionCache.friends.value)
        assertTrue(SessionCache.friendsFetchedAt > 0)
    }

    @Test
    fun `getFriends degrades gracefully when the weekly summary fetch fails`() = runTest {
        val client = fakeSupabaseClient { request ->
            val path = request.url.encodedPath
            val query = request.url.encodedQuery
            when {
                path.endsWith("/friends") && query.contains("requester_id=eq.") ->
                    respond("""[{"requester_id":"u1","receiver_id":"f1"}]""", headers = jsonHeaders)
                path.endsWith("/friends") -> respond("[]", headers = jsonHeaders)
                path.endsWith("/users") ->
                    respond("""[{"id":"f1","name":"Friend One","email":"f1@x.com"}]""", headers = jsonHeaders)
                path.endsWith("/user_streaks") -> respond("[]", headers = jsonHeaders)
                path.endsWith("/user_weekly_summary") -> respondError(HttpStatusCode.InternalServerError)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        val friends = repo.getFriends()

        assertEquals(1, friends.size)
        assertEquals(null, friends.first().weeklySummary)
    }

    @Test
    fun `getFriendRequests returns empty when nobody is logged in`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<FriendData>(), repo.getFriendRequests())
    }

    @Test
    fun `getFriendRequests maps pending requesters to FriendData`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/friend_requests") ->
                    respond("""[{"id":"r1","from_user_id":"f1","to_user_id":"u1"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") ->
                    respond("""[{"id":"f1","name":"Requester","email":"r@x.com"}]""", headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        val requests = repo.getFriendRequests()

        assertEquals(1, requests.size)
        assertEquals("Requester", requests.first().name)
    }

    @Test
    fun `getFriendRequests returns empty when there are no pending requests`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        assertEquals(emptyList<FriendData>(), repo.getFriendRequests())
    }

    @Test
    fun `getFriendRequests returns empty when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        assertEquals(emptyList<FriendData>(), repo.getFriendRequests())
    }

    @Test
    fun `acceptRequest deletes the pending request and inserts a friends row`() = runTest {
        val calls = mutableListOf<String>()
        val client = fakeSupabaseClient { request ->
            calls += "${request.method.value} ${request.url.encodedPath}"
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        repo.acceptRequest("f1")

        assertTrue(calls.any { it.contains("friend_requests") })
        assertTrue(calls.any { it.contains("/friends") })
    }

    @Test
    fun `acceptRequest is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.acceptRequest("f1")
    }

    @Test
    fun `rejectRequest deletes the pending request`() = runTest {
        val client = fakeSupabaseClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/friend_requests"))
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        repo.rejectRequest("f1")
    }

    @Test
    fun `reactToFriend upserts against the latest weekly summary`() = runTest {
        var captured: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/user_weekly_summary") ->
                    respond("""[{"id":"s1","user_id":"f1","week_start":"2026-07-20"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/reactions") -> {
                    captured = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        repo.reactToFriend("f1", ReactionType.entries.first())

        assertTrue(captured!!.contains("\"weekly_summary_id\":\"s1\""))
    }

    @Test
    fun `reactToFriend is a no-op when the friend has no weekly summary`() = runTest {
        val client = fakeSupabaseClient { respond("[]", headers = jsonHeaders) }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        repo.reactToFriend("f1", ReactionType.entries.first())
    }

    @Test
    fun `searchUsers returns empty for a blank query without hitting the network`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<Any>(), repo.searchUsers("  "))
    }

    @Test
    fun `searchUsers excludes the current user from results`() = runTest {
        val client = fakeSupabaseClient {
            respond(
                """[{"id":"u1","name":"Me","email":"me@x.com"},{"id":"f1","name":"Found","email":"f1@x.com"}]""",
                headers = jsonHeaders
            )
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        val results = repo.searchUsers("fo")

        assertEquals(1, results.size)
        assertEquals("f1", results.first().id)
    }

    @Test
    fun `searchUsers returns empty when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        assertEquals(emptyList<Any>(), repo.searchUsers("fo"))
    }

    @Test
    fun `addFriend inserts a friend request`() = runTest {
        val client = fakeSupabaseClient { request ->
            assertTrue(request.url.encodedPath.endsWith("/friend_requests"))
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        repo.addFriend("f1")
    }

    @Test
    fun `addFriend is a no-op when nobody is logged in`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        repo.addFriend("f1")
    }

    @Test
    fun `removeFriend deletes both directions of the friendship`() = runTest {
        val calls = mutableListOf<String>()
        val client = fakeSupabaseClient { request ->
            calls += request.url.encodedQuery
            respond("[]", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        repo.removeFriend("f1")

        assertEquals(2, calls.size)
    }

    @Test
    fun `getPendingSentRequestUserIds returns empty when nobody is logged in`() = runTest {
        val repo = SupabaseFriendRepository(fakeSupabaseClient { error("no HTTP expected") })
        assertEquals(emptyList<String>(), repo.getPendingSentRequestUserIds())
    }

    @Test
    fun `getPendingSentRequestUserIds maps sent requests to recipient ids`() = runTest {
        val client = fakeSupabaseClient {
            respond("""[{"to_user_id":"f1"},{"to_user_id":"f2"}]""", headers = jsonHeaders)
        }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        assertEquals(listOf("f1", "f2"), repo.getPendingSentRequestUserIds())
    }

    @Test
    fun `getPendingSentRequestUserIds returns empty when the request fails`() = runTest {
        val client = fakeSupabaseClient { respondError(HttpStatusCode.InternalServerError) }
        client.fakeLogin("u1")
        val repo = SupabaseFriendRepository(client)

        assertEquals(emptyList<String>(), repo.getPendingSentRequestUserIds())
    }
}
