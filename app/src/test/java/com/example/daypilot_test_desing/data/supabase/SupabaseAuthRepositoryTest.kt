package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.data.repository.RegisterOutcome
import com.example.daypilot_test_desing.support.SharedFakeSupabaseClient
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
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

private fun sessionJson(userId: String = "u1", extraUserFields: String = "") = """
    {"access_token":"tok","refresh_token":"rtok","expires_in":3600,"token_type":"bearer",
     "user":{"aud":"authenticated","id":"$userId"$extraUserFields}}
""".trimIndent()

private fun pendingConfirmationUserJson(userId: String = "u1") =
    """{"aud":"authenticated","id":"$userId"}"""

private fun MockRequestHandleScope.alreadyRegisteredErrorResponse() =
    respond(
        """{"msg":"User already registered"}""",
        status = HttpStatusCode.UnprocessableEntity,
        headers = jsonHeaders
    )

private fun MockRequestHandleScope.wrongPasswordErrorResponse() =
    respond(
        """{"msg":"Invalid login credentials"}""",
        status = HttpStatusCode.BadRequest,
        headers = jsonHeaders
    )

class SupabaseAuthRepositoryTest {

    companion object {
        private val shared = SharedFakeSupabaseClient()
    }

    private fun fakeSupabaseClient(
        handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
    ) = shared.use(handler)

    @Before
    fun setUp() = runBlocking {
        shared.resetAuth()
    }

    @Test
    fun `login signs in and creates a missing profile row`() = runTest {
        var insertedBody: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/token") -> respond(sessionJson(), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post -> {
                    insertedBody = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        repo.login("a@x.com", "password123")

        assertTrue(insertedBody!!.contains("\"id\":\"u1\""))
    }

    @Test
    fun `login skips profile creation when the row already exists`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/token") -> respond(sessionJson(), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("""[{"id":"u1"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post ->
                    error("should not insert when a profile row already exists")
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        repo.login("a@x.com", "password123")
    }

    @Test
    fun `register creates the account and profile row on a fresh signup`() = runTest {
        var insertedBody: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") -> respond(sessionJson(), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post -> {
                    insertedBody = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        val outcome = repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")

        assertEquals(RegisterOutcome.Success, outcome)
        assertTrue(insertedBody!!.contains("\"username_lower\":\"ana\""))
    }

    @Test
    fun `register returns pending confirmation when signup yields no session`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") ->
                    respond(pendingConfirmationUserJson(), headers = jsonHeaders)
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        val outcome = repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")

        assertEquals(RegisterOutcome.PendingEmailConfirmation, outcome)
    }

    @Test
    fun `register returns already exists when a profile row is already present`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") -> respond(sessionJson(), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("""[{"id":"u1"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post ->
                    error("should not insert when a profile row already exists")
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        val outcome = repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")

        assertEquals(RegisterOutcome.AlreadyExists, outcome)
    }

    @Test
    fun `register falls back to sign-in on an already-registered account and finishes the profile`() = runTest {
        var insertedBody: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") -> alreadyRegisteredErrorResponse()
                request.url.encodedPath.endsWith("/token") -> respond(sessionJson(), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post -> {
                    insertedBody = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        val outcome = repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")

        assertEquals(RegisterOutcome.Success, outcome)
        assertTrue(insertedBody!!.contains("\"id\":\"u1\""))
    }

    @Test
    fun `register falls back to sign-in on an already-registered account with an existing profile`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") -> alreadyRegisteredErrorResponse()
                request.url.encodedPath.endsWith("/token") -> respond(sessionJson(), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("""[{"id":"u1"}]""", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post ->
                    error("should not insert when a profile row already exists")
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        val outcome = repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")

        assertEquals(RegisterOutcome.AlreadyExists, outcome)
    }

    @Test
    fun `register returns already exists when the sign-in fallback fails with the wrong password`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") -> alreadyRegisteredErrorResponse()
                request.url.encodedPath.endsWith("/token") -> wrongPasswordErrorResponse()
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        val outcome = repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")

        assertEquals(RegisterOutcome.AlreadyExists, outcome)
    }

    @Test
    fun `register rethrows signup failures unrelated to an existing account`() = runTest {
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/signup") ->
                    respond(
                        """{"msg":"Signups not allowed for this instance"}""",
                        status = HttpStatusCode.BadRequest,
                        headers = jsonHeaders
                    )
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        try {
            repo.register("Ana", "ana", "a@x.com", "password123", "europe_madrid")
            fail("expected an exception to propagate")
        } catch (e: Exception) {
            assertTrue(e.message?.contains("Signups not allowed", ignoreCase = true) == true)
        }
    }

    @Test
    fun `sendResetEmail posts the recovery request`() = runTest {
        var requestedPath: String? = null
        val client = fakeSupabaseClient { request ->
            requestedPath = request.url.encodedPath
            respond("{}", headers = jsonHeaders)
        }
        val repo = SupabaseAuthRepository(client)

        repo.sendResetEmail("a@x.com")

        assertTrue(requestedPath!!.endsWith("/recover"))
    }

    @Test
    fun `ensureProfileExists falls back to metadata for name, username and region`() = runTest {
        var insertedBody: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/token") -> respond(
                    sessionJson(
                        extraUserFields = ""","email":"ana@x.com","user_metadata":{"name":"Ana Garcia","username":"anag","region":"europe_madrid"}"""
                    ),
                    headers = jsonHeaders
                )
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post -> {
                    insertedBody = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        repo.login("ana@x.com", "password123")

        assertTrue(insertedBody!!.contains("\"name\":\"Ana Garcia\""))
        assertTrue(insertedBody!!.contains("\"username\":\"anag\""))
        assertTrue(insertedBody!!.contains("\"region\":\"europe_madrid\""))
    }

    @Test
    fun `ensureProfileExists derives username and name from the email when metadata is missing`() = runTest {
        var insertedBody: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/token") -> respond(
                    sessionJson(extraUserFields = ""","email":"noMeta@x.com""""),
                    headers = jsonHeaders
                )
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post -> {
                    insertedBody = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        repo.login("noMeta@x.com", "password123")

        assertTrue(insertedBody!!.contains("\"username\":\"noMeta\""))
        assertTrue(insertedBody!!.contains("\"name\":\"noMeta\""))
        assertTrue(insertedBody!!.contains("\"region\":\"\""))
    }

    @Test
    fun `ensureProfileExists falls back to the user id when there is no email or username metadata`() = runTest {
        var insertedBody: String? = null
        val client = fakeSupabaseClient { request ->
            when {
                request.url.encodedPath.endsWith("/token") -> respond(sessionJson(userId = "u-no-email"), headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Get ->
                    respond("[]", headers = jsonHeaders)
                request.url.encodedPath.endsWith("/users") && request.method == HttpMethod.Post -> {
                    insertedBody = String(request.body.toByteArray())
                    respond("[]", headers = jsonHeaders)
                }
                else -> respond("[]", headers = jsonHeaders)
            }
        }
        val repo = SupabaseAuthRepository(client)

        repo.login("whatever@x.com", "password123")

        assertTrue(insertedBody!!.contains("\"username\":\"u-no-email\""))
        assertTrue(insertedBody!!.contains("\"email\":\"\""))
    }
}
