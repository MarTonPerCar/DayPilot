package com.example.daypilot.mainDatabase

import android.content.Context
import androidx.core.content.edit

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    private val keyDarkMode = "dark_mode"
    private val keyLanguage = "language"
    private val keyNotifications = "notifications_enabled"

    companion object {
        private const val KEY_LOGGED_IN = "logged_in"
    }

    fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit {
            putBoolean(KEY_LOGGED_IN, loggedIn)
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_LOGGED_IN, false)
    }

    fun logout() {
        prefs.edit {
            putBoolean(KEY_LOGGED_IN, false)
        }
    }

    // ===== Modo Oscuro =======

    fun isDarkModeEnabled(): Boolean {
        return prefs.getBoolean(keyDarkMode, false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(keyDarkMode, enabled) }
    }

    // ===== Idiomas =======

    fun getLanguage(): String {
        return prefs.getString(keyLanguage, "es") ?: "es"
    }

    fun setLanguage(lang: String) {
        prefs.edit { putString(keyLanguage, lang) }
    }

    // ===== Notificaciones =======

    fun areNotificationsEnabled(): Boolean {
        return prefs.getBoolean(keyNotifications, true)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(keyNotifications, enabled) }
    }
}