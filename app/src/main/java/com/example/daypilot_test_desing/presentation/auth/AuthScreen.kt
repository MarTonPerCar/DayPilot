package com.example.daypilot_test_desing.presentation.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.forms.AuthToggle
import com.example.daypilot_test_desing.ui.components.forms.LoginCard
import com.example.daypilot_test_desing.ui.components.forms.RegisterCard

@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    isLoginLoading: Boolean = false,
    isRegisterLoading: Boolean = false,
    loginError: String = "",
    registerError: String = "",
    onLoginClick: (email: String, password: String) -> Unit = { _, _ -> },
    onRegisterClick: (name: String, username: String, email: String, password: String, region: String) -> Unit = { _, _, _, _, _ -> }
) {
    var isLogin by remember { mutableStateOf(true) }

    val rotation by animateFloatAsState(
        targetValue = if (isLogin) 0f else 180f,
        animationSpec = tween(durationMillis = 600),
        label = "card_flip"
    )

    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp)
        ) {
            // ── Logo ─────────────────────────────────────────────
            Image(
                painter = painterResource(
                    id = if (isDark) R.drawable.mi_logo_blanco
                    else R.drawable.mi_logo_negro
                ),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.height(60.dp)
            )

            // ── Toggle ───────────────────────────────────────────
            AuthToggle(isLogin = isLogin, onToggle = { isLogin = it })

            // ── Cards ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
            ) {
                if (rotation <= 90f) {
                    LoginCard(
                        isLoading = isLoginLoading,
                        errorMessage = loginError,
                        onLogin = onLoginClick
                    )
                }

                if (rotation > 90f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { rotationY = 180f }
                    ) {
                        RegisterCard(
                            isLoading = isRegisterLoading,
                            errorMessage = registerError,
                            onRegister = { name, username, email, password, region ->
                                onRegisterClick(name, username, email, password, region)
                                isLogin = true
                            },
                        )
                    }
                }
            }
        }
    }
}
