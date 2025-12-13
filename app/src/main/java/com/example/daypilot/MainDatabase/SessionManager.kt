package com.example.daypilot.MainDatabase

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOGGED_IN = "logged_in"
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit()
            .putBoolean(KEY_LOGGED_IN, loggedIn)
            .apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    fun logout() {
        setLoggedIn(false)
    }
}