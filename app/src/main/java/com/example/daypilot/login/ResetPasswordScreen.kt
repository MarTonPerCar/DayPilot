package com.example.daypilot.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import com.example.daypilot.R
import com.example.daypilot.mainDatabase.SessionManager

@Composable
fun ResetPasswordScreen(
    initialEmail: String,
    isLoading: Boolean,
    errorMessage: String?,
    success: Boolean,
    onBackClick: () -> Unit,
    onSendClick: (String) -> Unit
) {
    var email by remember { mutableStateOf(initialEmail) }
    var emailError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Agendas de fondo
            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-40).dp)
                    .rotate(20f)
                    .alpha(0.12f)
            )

            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-60).dp, y = (-30).dp)
                    .rotate(-15f)
                    .alpha(0.08f)
            )

            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(210.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-50).dp, y = 40.dp)
                    .rotate(12f)
                    .alpha(0.06f)
            )

            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 60.dp)
                    .rotate(-10f)
                    .alpha(0.08f)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Recuperar contraseña", style = MaterialTheme.typography.headlineMedium)

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = emailError != null
                )

                if (emailError != null) {
                    Text(
                        text = emailError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (success) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Te hemos enviado un correo para restablecer la contraseña.",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (email.isBlank()) {
                            emailError = "Introduce tu email."
                        } else {
                            onSendClick(email)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text("Enviar correo de recuperación")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onBackClick) {
                    Text("Volver")
                }
            }
        }
    }
}