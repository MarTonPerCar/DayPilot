package com.example.daypilot_test_desing.support

import androidx.test.core.app.ApplicationProvider
import com.russhwolf.settings.SettingsInitializer

/** The global `supabase` client's Auth plugin creates a SharedPreferences-backed
 *  SettingsSessionManager on construction. In a real app that context comes from an
 *  androidx.startup ContentProvider that fires automatically at process start; under
 *  Robolectric that never runs, so referencing `supabase` at all throws
 *  IllegalStateException("Failed to create default settings...") unless this is called
 *  first — this is the initializer's own documented test entry point.
 *
 *  Since no real session is ever persisted in this simulated environment, every
 *  ViewModel's `supabase.auth.currentUserOrNull()` naturally returns null afterward,
 *  so realtime-subscription code paths short-circuit exactly like production behavior
 *  for a fresh/unauthenticated process — no further Supabase mocking is needed. */
fun initSupabaseSettingsForTest() {
    SettingsInitializer().create(ApplicationProvider.getApplicationContext())
}
