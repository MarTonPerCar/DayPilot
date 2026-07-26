package com.example.daypilot_test_desing.support

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences

/** A plain-JVM [Context] stand-in for the handful of call sites (NotificationHub, etc.) that
 *  only ever call [getSharedPreferences] / [getApplicationContext] — avoids needing Robolectric
 *  (and its real Application) just to satisfy that signature. Robolectric-run classes aren't
 *  visible to JaCoCo's coverage agent at all, so avoiding it keeps coverage real. Any other
 *  Context method being called is a sign this fake needs to grow, not that it's wrong to use. */
class FakeContext(
    private val prefs: SharedPreferences = FakeSharedPreferences()
) : ContextWrapper(null) {
    override fun getApplicationContext(): Context = this
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = prefs
    override fun getPackageName(): String = "com.example.daypilot_test_desing"

    // No system services in a plain-JVM test — callers are expected to null-check
    // (context.getSystemService(AlarmManager::class.java) ?: return), same as a real device
    // without that service.
    override fun getSystemService(name: String): Any? = null
}
