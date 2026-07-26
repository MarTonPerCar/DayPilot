package com.example.daypilot_test_desing.support

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

/** A plain-JVM [Application] stand-in for `AndroidViewModel(application)` constructors — same
 *  minimal surface as [FakeContext], for the handful of call sites that need an actual
 *  Application instance rather than any Context. Robolectric-run classes aren't visible to
 *  JaCoCo's coverage agent at all, so avoiding it keeps coverage real. */
class FakeApplication(
    private val prefs: SharedPreferences = FakeSharedPreferences(),
    private val systemServices: Map<String, Any?> = emptyMap()
) : Application() {
    override fun getApplicationContext(): Context = this
    override fun getSharedPreferences(name: String?, mode: Int): SharedPreferences = prefs
    override fun getPackageName(): String = "com.example.daypilot_test_desing"
    override fun getSystemService(name: String): Any? = systemServices[name]
}
