package com.example.daypilot_test_desing.ui.components.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotButtonPrimary
import com.example.daypilot_test_desing.ui.components.basic.DayPilotButtonText
import com.example.daypilot_test_desing.ui.components.basic.DayPilotDropdownField
import com.example.daypilot_test_desing.ui.components.basic.DayPilotPasswordField
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTextField
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

@Composable
fun AuthToggle(isLogin: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ToggleOption(
            text = "Iniciar sesión",
            selected = isLogin,
            onClick = { onToggle(true) }
        )
        ToggleOption(
            text = "Crear cuenta",
            selected = !isLogin,
            onClick = { onToggle(false) }
        )
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
            .padding(horizontal = 20.dp, vertical = 10.dp),
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
fun LoginCard(
    isLoading: Boolean = false,
    errorMessage: String = "",
    onLogin: (email: String, password: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AuthCard {
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(4.dp))

        DayPilotTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = errorMessage.isNotEmpty(),
        )

        DayPilotPasswordField(
            value = password,
            onValueChange = { password = it },
            isError = errorMessage.isNotEmpty(),
            errorMessage = errorMessage
        )

        DayPilotButtonText(
            text = "¿Has olvidado tu contraseña?",
            onClick = {},
            modifier = Modifier.align(Alignment.End)
        )

        DayPilotButtonPrimary(
            text = "Entrar",
            onClick = { onLogin(email, password) },
            isLoading = isLoading,
            hasError = errorMessage.isNotEmpty()
        )
    }
}

// ── Register Card ────────────────────────────────────────────────
@Composable
fun RegisterCard(
    isLoading: Boolean = false,
    errorMessage: String = "",
    onRegister: (name: String, username: String, email: String, password: String, region: String) -> Unit,
    onSuccess: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("Europe/Madrid") }

    val regions = listOf(
        "Europe/Madrid",
        "Atlantic/Canary",
        "America/New_York",
        "America/Los_Angeles",
        "America/Mexico_City",
        "America/Sao_Paulo",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Australia/Sydney"
    )

    AuthCard {
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(4.dp))

        DayPilotTextField(
            value = name,
            onValueChange = { name = it },
            label = "Nombre"
        )

        DayPilotTextField(
            value = username,
            onValueChange = { username = it },
            label = "Nombre de usuario"
        )

        DayPilotDropdownField(
            value = region,
            options = regions,
            onSelect = { region = it },
            label = "Región / zona horaria",
            displayText = { it }
        )

        DayPilotTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            keyboardType = KeyboardType.Email,
            isError = errorMessage.isNotEmpty()
        )

        DayPilotPasswordField(
            value = password,
            onValueChange = { password = it },
            isError = errorMessage.isNotEmpty(),
            errorMessage = errorMessage
        )

        DayPilotButtonPrimary(
            text = "Registrar",
            onClick = { onRegister(name, username, email, password, region) },
            isLoading = isLoading
        )
    }
}

// ── AuthCard base ────────────────────────────────────────────────
@Composable
fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthComponentsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AuthToggle(isLogin = true, onToggle = {})
            LoginCard(onLogin = { _, _ -> })
        }
    }
}