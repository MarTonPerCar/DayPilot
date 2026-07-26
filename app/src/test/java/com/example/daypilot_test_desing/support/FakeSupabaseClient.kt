package com.example.daypilot_test_desing.support

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.resumable.MemoryResumableCache
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.coroutines.Dispatchers

private val fakeClientCounter = java.util.concurrent.atomic.AtomicLong()

/** Builds a real SupabaseClient wired to [handler] instead of a live network connection, so
 *  repository tests exercise real query construction, response mapping, and error handling —
 *  the same "mock at the transport layer" approach used for the Flutter repository tests.
 *
 *  Two deliberate anti-flakiness choices, found empirically by running many repository test
 *  classes together and seeing occasional unrelated assertion failures:
 *  - `dispatcher = Dispatchers.Unconfined`: without it, MockEngine dispatches request handling
 *    onto its own internal (real) dispatcher, which races against runTest's virtual scheduler
 *    once enough SupabaseClients pile up across a full test run.
 *  - a unique fake URL per client: MockEngine.close() doesn't block on its internal Job actually
 *    finishing (it just registers a completion callback and returns), so a still-closing client
 *    from a previous test could otherwise collide with a new one if anything were keyed by URL. */
fun fakeSupabaseClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
): SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://fake-${fakeClientCounter.incrementAndGet()}.supabase.co",
    supabaseKey = "fake-key"
) {
    httpEngine = MockEngine(MockEngineConfig().apply {
        addHandler { request -> handler(request) }
        dispatcher = Dispatchers.Unconfined
    })
    install(Postgrest)
    // Without a Context (no Robolectric), Storage's default resumable cache tries to build an
    // Android SharedPreferences-backed Settings() and NPEs — swap it for an in-memory one.
    install(Storage) { resumable { cache = MemoryResumableCache() } }
    install(Auth) { minimalConfig() }
}

/** Locally imports a fake authenticated session — no network call, so `auth.currentUserOrNull()`
 *  returns a real user without needing to mock a login request/response. */
suspend fun SupabaseClient.fakeLogin(userId: String = "u1") {
    auth.importSession(
        session = UserSession(
            accessToken = "fake-access-token",
            refreshToken = "fake-refresh-token",
            expiresIn = 3600,
            tokenType = "bearer",
            user = UserInfo(aud = "authenticated", id = userId)
        ),
        autoRefresh = false
    )
}

/** Holds ONE real SupabaseClient (and Ktor engine) shared across every test in a class, swapping
 *  the mock handler per test instead of constructing a new client each time. Put one in a test
 *  class's `companion object` and reset it (`resetAuth()`) in `@Before`.
 *
 *  Found empirically that constructing dozens of real clients across a full suite run — one per
 *  test, across many repository test classes — causes rare cross-test assertion failures. */
class SharedFakeSupabaseClient {
    private var handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData =
        { error("no handler set for this test") }

    val client: SupabaseClient by lazy { fakeSupabaseClient { request -> handler(request) } }

    fun use(block: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): SupabaseClient {
        handler = block
        return client
    }

    suspend fun resetAuth() {
        client.auth.clearSession()
    }
}
