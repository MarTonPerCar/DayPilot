package com.example.daypilot.mainDatabase

import android.content.Context
import android.content.Intent
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.login.LoginActivity

object SessionGuard {
    fun ensureValidSessionOrLogout(
        context: Context,
        sessionManager: SessionManager,
        authRepo: AuthRepository
    ): Boolean {
        val localLogged = sessionManager.isLoggedIn()
        val firebaseUser = authRepo.currentUser

        if (localLogged && firebaseUser == null) {
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
