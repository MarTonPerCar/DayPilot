package com.example.daypilot.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.daypilot.R

// ---------------------------
// MAPEADOR DE ERRORES DE FIREBASE
// ---------------------------

data class LoginFieldErrors(
    val emailError: String? = null,
    val passError: String? = null
)

fun mapFirebaseErrorToFieldErrors(errorCode: String): LoginFieldErrors {
    return when (errorCode.uppercase()) {

        "ERROR_INVALID_EMAIL" ->
            LoginFieldErrors(emailError = "El email no es válido.")

        "ERROR_USER_NOT_FOUND" ->
            LoginFieldErrors(emailError = "No existe ninguna cuenta con ese email.")

        "ERROR_WRONG_PASSWORD" ->
            LoginFieldErrors(passError = "La contraseña es incorrecta.")

        "ERROR_USER_DISABLED" ->
            LoginFieldErrors(emailError = "La cuenta está deshabilitada.")

        "ERROR_TOO_MANY_REQUESTS" ->
            LoginFieldErrors(passError = "Demasiados intentos. Intenta más tarde.")

        "ERROR_INVALID_CREDENTIAL" ->
            LoginFieldErrors(passError = "El usuario o contraseña no son válidos.")

        else ->
            LoginFieldErrors(emailError = errorCode)
    }
}

// ---------------------------
// LOGIN SCREEN
// ---------------------------

@Composable
fun LoginScreen(
    darkTheme: Boolean,
    onLoginClick: (String, String) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotPasswordClick: (String) -> Unit,
    firebaseErrorCode: String?,
    isLoading: Boolean
) {
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Errores de validación local
    var localEmailError by remember { mutableStateOf<String?>(null) }
    var localPassError by remember { mutableStateOf<String?>(null) }

    // Errores de Firebase mapeados
    val firebaseFieldErrors = remember(firebaseErrorCode) {
        firebaseErrorCode?.let { mapFirebaseErrorToFieldErrors(it) }
    }

    val emailError = localEmailError ?: firebaseFieldErrors?.emailError
    val passError = localPassError ?: firebaseFieldErrors?.passError

    val logoRes = if (darkTheme) R.drawable.mi_logo_blanco else R.drawable.mi_logo_negro

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // === AGENDA 1: esquina superior izquierda ===
            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(220.dp)
                    .align(Alignment.TopStart)
                    .offset(x = (-70).dp, y = (-40).dp)
                    .rotate(-18f)
                    .alpha(0.10f)
            )

            // === AGENDA 2: esquina superior derecha ===
            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-20).dp)
                    .rotate(15f)
                    .alpha(0.08f)
            )

            // === AGENDA 3: esquina inferior izquierda ===
            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(200.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-60).dp, y = 40.dp)
                    .rotate(12f)
                    .alpha(0.06f)
            )

            // === AGENDA 4: esquina inferior derecha ===
            Image(
                painter = painterResource(id = R.drawable.agenda),
                contentDescription = null,
                modifier = Modifier
                    .size(210.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 30.dp, y = 60.dp)
                    .rotate(-10f)
                    .alpha(0.08f)
            )

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = logoRes),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .padding(bottom = 8.dp)
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

                        Spacer(modifier = Modifier.height(16.dp))

                        // EMAIL
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                localEmailError = null
                            },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError != null,
                            singleLine = true
                        )
                        if (emailError != null) {
                            Text(
                                text = emailError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PASSWORD
                        OutlinedTextField(
                            value = pass,
                            onValueChange = {
                                pass = it
                                localPassError = null
                            },
                            label = { Text("Contraseña") },
                            visualTransformation = if (showPassword)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword)
                                            Icons.Filled.VisibilityOff
                                        else
                                            Icons.Filled.Visibility,
                                        contentDescription = "Mostrar contraseña"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            isError = passError != null,
                            singleLine = true
                        )
                        if (passError != null) {
                            Text(
                                text = passError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { onForgotPasswordClick(email) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("¿Has olvidado tu contraseña?")
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                localEmailError = null
                                localPassError = null

                                var hasError = false

                                if (email.isBlank()) {
                                    localEmailError = "Introduce tu email."
                                    hasError = true
                                }
                                if (pass.isBlank()) {
                                    localPassError = "Introduce tu contraseña."
                                    hasError = true
                                }

                                if (!hasError) {
                                    onLoginClick(email, pass)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Entrar")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = onRegisterClick) {
                            Text("Crear cuenta")
                        }
                    }
                }
            }
        }
    }
}