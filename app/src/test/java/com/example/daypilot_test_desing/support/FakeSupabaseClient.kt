package com.example.daypilot_test_desing.support

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.minimalConfig
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData

/** Builds a real SupabaseClient wired to [handler] instead of a live network connection, so
 *  repository tests exercise real query construction, response mapping, and error handling —
 *  the same "mock at the transport layer" approach used for the Flutter repository tests. */
fun fakeSupabaseClient(
    handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData
): SupabaseClient = createSupabaseClient(
    supabaseUrl = "https://fake.supabase.co",
    supabaseKey = "fake-key"
) {
    httpEngine = MockEngine(handler)
    install(Postgrest)
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
