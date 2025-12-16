package com.example.daypilot.mainDatabase

import android.content.Context
import android.content.Intent
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.login.LoginActivity

object SessionGuard {

    /**
     * Si SharedPrefs dice "logged_in" pero Firebase no tiene user,
     * forzamos logout para evitar softlock y redirigimos a Login.
     *
     * Devuelve true si la sesión es válida, false si se ha forzado logout.
     */
    fun ensureValidSessionOrLogout(
        context: Context,
        sessionManager: SessionManager,
        authRepo: AuthRepository
    ): Boolean {
        val localLogged = sessionManager.isLoggedIn()
        val firebaseUser = authRepo.currentUser

        if (localLogged && firebaseUser == null) {
            // Estado inconsistente -> hard logout
            sessionManager.logout()
            authRepo.logout()

            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
            return false
        }

        return firebaseUser != null
    }
}
