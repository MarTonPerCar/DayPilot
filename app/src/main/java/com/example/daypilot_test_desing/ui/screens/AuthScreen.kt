package com.example.daypilot_test_desing.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotButtonPrimary

@Composable
fun AuthScreen() {
    var isLogin by remember { mutableStateOf(true) }

    val rotation by animateFloatAsState(
        targetValue = if (isLogin) 0f else 180f,
        animationSpec = tween(durationMillis = 600),
        label = "card_flip"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            AuthToggle(isLogin = isLogin, onToggle = { isLogin = it })

            // ── Ambas cards siempre en el árbol ──────────────────
            Box(
                modifier = Modifier
                    .width(320.dp)
                    .height(380.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
            ) {
                // Front — Login (visible cuando rotation < 90)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = if (rotation <= 90f) 1f else 0f
                        }
                ) {
                    LoginCard()
                }

                // Back — Register (visible cuando rotation > 90, girada 180)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationY = 180f
                            alpha = if (rotation > 90f) 1f else 0f
                        }
                ) {
                    RegisterCard()
                }
            }
        }
    }
}

// ── Toggle ───────────────────────────────────────────────────────
@Composable
fun AuthToggle(isLogin: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToggleOption(text = "Log in",  selected = isLogin,  onClick = { onToggle(true) })
        ToggleOption(text = "Sign up", selected = !isLogin, onClick = { onToggle(false) })
    }
}

@Composable
fun ToggleOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Login Card ───────────────────────────────────────────────────
@Composable
fun LoginCard() {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthCard {
        Text("Log in", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        AuthTextField(value = email,    onValueChange = { email = it },    placeholder = "Email",    keyboardType = KeyboardType.Email)
        AuthTextField(value = password, onValueChange = { password = it }, placeholder = "Password", isPassword = true)
        Spacer(Modifier.height(4.dp))
        DayPilotButtonPrimary(
            text = "Let's go!",
            onClick = {},
            isLoading = false,
            hasError = false
        )
    }
}

// ── Register Card ────────────────────────────────────────────────
@Composable
fun RegisterCard() {
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthCard {
        Text("Sign up", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        AuthTextField(value = name,     onValueChange = { name = it },     placeholder = "Name")
        AuthTextField(value = email,    onValueChange = { email = it },    placeholder = "Email",    keyboardType = KeyboardType.Email)
        AuthTextField(value = password, onValueChange = { password = it }, placeholder = "Password", isPassword = true)
        Spacer(Modifier.height(4.dp))
        AuthButton(text = "Confirm!", onClick = {})
    }
}

// ── Componentes base ─────────────────────────────────────────────
@Composable
fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor      = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
            focusedContainerColor   = MaterialTheme.colorScheme.background,
            unfocusedContainerColor = MaterialTheme.colorScheme.background,
            focusedTextColor        = MaterialTheme.colorScheme.onBackground,
            unfocusedTextColor      = MaterialTheme.colorScheme.onBackground,
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun AuthButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}