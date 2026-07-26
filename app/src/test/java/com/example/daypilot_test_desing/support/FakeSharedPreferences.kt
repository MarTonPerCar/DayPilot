package com.example.daypilot_test_desing.support

import android.content.SharedPreferences

/** A plain-JVM in-memory [SharedPreferences], so repository tests don't need Robolectric (and its
 *  real Android Context) just to get a working prefs instance — Robolectric-run classes aren't
 *  visible to JaCoCo's coverage agent at all, so avoiding it here keeps this repository's
 *  coverage real. Only implements what the app's repositories actually call. */
class FakeSharedPreferences : SharedPreferences {

    private val values = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = values

    override fun getString(key: String, defValue: String?) = values[key] as? String ?: defValue

    override fun getStringSet(key: String, defValues: MutableSet<String>?) =
        @Suppress("UNCHECKED_CAST") (values[key] as? MutableSet<String> ?: defValues)

    override fun getInt(key: String, defValue: Int) = values[key] as? Int ?: defValue

    override fun getLong(key: String, defValue: Long) = values[key] as? Long ?: defValue

    override fun getFloat(key: String, defValue: Float) = values[key] as? Float ?: defValue

    override fun getBoolean(key: String, defValue: Boolean) = values[key] as? Boolean ?: defValue

    override fun contains(key: String) = values.containsKey(key)

    override fun edit(): SharedPreferences.Editor = FakeEditor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) = Unit

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) = Unit

    private inner class FakeEditor : SharedPreferences.Editor {
        private val pending = mutableMapOf<String, Any?>()
        private var cleared = false

        override fun putString(key: String, value: String?) = apply { pending[key] = value }
        override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values }
        override fun putInt(key: String, value: Int) = apply { pending[key] = value }
        override fun putLong(key: String, value: Long) = apply { pending[key] = value }
        override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
        override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
        override fun remove(key: String) = apply { pending[key] = REMOVED }
        override fun clear() = apply { cleared = true }

        override fun commit(): Boolean {
            apply()
            return true
        }

        override fun apply() {
            if (cleared) values.clear()
            pending.forEach { (key, value) ->
                if (value === REMOVED) values.remove(key) else values[key] = value
            }
        }
    }

    private companion object {
        val REMOVED = Any()
    }
}
