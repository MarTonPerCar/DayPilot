package com.example.daypilot_test_desing.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Coordinates session lifecycle for the whole app.
 *
 * On startup it checks whether supabase-kt restored a saved session from
 * SharedPreferences (the Auth plugin does this automatically). If one exists
 * the app skips the auth screen and loads all user data immediately.
 *
 * Session persistence note: supabase-kt stores the JWT + refresh token in
 * Android SharedPreferences automatically. JWTs expire after ~1 hour and are
 * silently refreshed by the plugin. Refresh tokens last ~7 days, so users
 * stay logged in without any extra work on our side.
 */
class AppSessionViewModel : ViewModel() {

    sealed class State {
        data object Loading : State()
        data object DataLoading : State()  // session confirmed, waiting for data fetch
        data object DataLoadFailed : State()  // startup data fetch failed twice, waiting for manual retry
        data object Authenticated : State()
        data object Unauthenticated : State()
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.value = try {
                // Wait until the Auth plugin has finished loading the persisted session
                // from SharedPreferences (via supabase-kt's SettingsSessionManager).
                // Without this, currentUserOrNull() always returns null on a cold start
                // because the async storage load hasn't completed yet.
                supabase.auth.awaitInitialization()
                if (supabase.auth.currentUserOrNull() != null) State.DataLoading
                else State.Unauthenticated
            } catch (_: Exception) {
                State.Unauthenticated
            }
        }
    }

    /** Call immediately after a successful login or registration. */
    fun notifyAuthenticated() {
        _state.value = State.DataLoading
    }

    /** Call after all ViewModel data has been loaded; triggers navigation to HOME. */
    fun markDataLoaded() {
        _state.value = State.Authenticated
    }

    /** Call when the startup data fetch failed even after an automatic retry. */
    fun markDataLoadFailed() {
        _state.value = State.DataLoadFailed
    }

    /** User tapped retry on the data-load-failed screen; re-enters DataLoading. */
    fun retryDataLoad() {
        _state.value = State.DataLoading
    }

    /** Signs the user out of Supabase and marks the session as gone. */
    fun signOut() {
        viewModelScope.launch {
            try { supabase.auth.signOut() } catch (_: Exception) {}
            SessionCache.clear()
            NotificationHub.clear()
            _state.value = State.Unauthenticated
        }
    }
}
