package com.example.daypilot_test_desing.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetPasswordScreen(
    isLoading: Boolean = false,
    isSuccess: Boolean = false,
    errorMessage: String = "",
    onSendResetEmail: (email: String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.reset_password_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text     = "🔐",
                    style    = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Text(
                text      = stringResource(R.string.reset_password_description),
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (isSuccess) {
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Text(
                        text       = stringResource(R.string.reset_password_success),
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary,
                        textAlign  = TextAlign.Center,
                        modifier   = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            DayPilotTextField(
                value         = email,
                onValueChange = { email = it },
                label         = stringResource(R.string.email),
                keyboardType  = KeyboardType.Email,
                isError       = errorMessage.isNotEmpty(),
                errorMessage  = errorMessage,
                enabled       = !isSuccess
            )

            DayPilotButtonPrimary(
                text      = stringResource(R.string.reset_password_send),
                onClick   = { onSendResetEmail(email) },
                isLoading = isLoading,
                enabled   = !isSuccess
            )

            DayPilotButtonText(
                text    = stringResource(R.string.back_to_login),
                onClick = onBack
            )
        }
    }
}