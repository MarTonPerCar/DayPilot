package com.example.daypilot.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import com.example.daypilot.R
import java.util.TimeZone
import kotlin.math.abs

data class TimeZoneDisplay(
    val id: String,
    val label: String
)

data class RegionOption(
    val id: String,    // ej: "Europe/Madrid"
    val label: String  // ej: "Europe/Madrid (UTC+01:00)"
)

// Lista fija de regiones habituales
private val regionOptions = listOf(
    RegionOption("Europe/Madrid", "Europe/Madrid (UTC+01:00)"),
    RegionOption("Atlantic/Canary", "Atlantic/Canary (UTC+00:00)"),
    RegionOption("Europe/London", "Europe/London (UTC+00:00)"),
    RegionOption("Europe/Paris", "Europe/Paris (UTC+01:00)"),
    RegionOption("America/New_York", "America/New_York (UTC-05:00)"),
    RegionOption("America/Los_Angeles", "America/Los_Angeles (UTC-08:00)"),
    RegionOption("America/Mexico_City", "America/Mexico_City (UTC-06:00)"),
    RegionOption("America/Sao_Paulo", "America/Sao_Paulo (UTC-03:00)"),
    RegionOption("Asia/Tokyo", "Asia/Tokyo (UTC+09:00)"),
    RegionOption("Asia/Shanghai", "Asia/Shanghai (UTC+08:00)"),
    RegionOption("Asia/Dubai", "Asia/Dubai (UTC+04:00)"),
    RegionOption("Australia/Sydney", "Australia/Sydney (UTC+10:00)")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, String, String, String) -> Unit,
    // name, username, email, pass, regionZoneId
    onBackToLogin: () -> Unit,
    isLoading: Boolean
) {
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    // Región seleccionada
    var expanded by remember { mutableStateOf(false) }
    var selectedRegionId by remember { mutableStateOf<String?>(null) }
    var regionDisplay by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }

    // Errores por campo
    var nameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passError by remember { mutableStateOf<String?>(null) }
    var regionError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // AGENDA 1: arriba derecha
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

            // AGENDA 2: arriba izquierda
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

            // AGENDA 3: abajo izquierda
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

            // AGENDA 4: abajo derecha
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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Image(
                    painter = painterResource(id = R.drawable.mi_logo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 16.dp)
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

                        Text("Crear cuenta", style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.height(16.dp))

                        // ---- NOMBRE ----
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                nameError = null
                            },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameError != null,
                            singleLine = true
                        )
                        if (nameError != null) {
                            Text(
                                text = nameError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ---- USERNAME ----
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                username = it
                                usernameError = null
                            },
                            label = { Text("Nombre de usuario") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = usernameError != null,
                            singleLine = true
                        )
                        if (usernameError != null) {
                            Text(
                                text = usernameError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ---- REGIÓN / ZONA HORARIA (COMBOBOX) ----
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = regionDisplay,
                                onValueChange = { /* readOnly */ },
                                readOnly = true,
                                label = { Text("Región / zona horaria") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()   // el warning es solo deprecado, pero funciona
                                    .fillMaxWidth(),
                                isError = regionError != null,
                                singleLine = true
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                regionOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            selectedRegionId = option.id
                                            regionDisplay = option.label
                                            regionError = null
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                        if (regionError != null) {
                            Text(
                                text = regionError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ---- EMAIL ----
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                emailError = null
                            },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailError != null,
                            singleLine = true
                        )
                        if (emailError != null) {
                            Text(
                                text = emailError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ---- CONTRASEÑA ----
                        OutlinedTextField(
                            value = pass,
                            onValueChange = {
                                pass = it
                                passError = null
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
                                text = passError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                nameError = null
                                usernameError = null
                                emailError = null
                                passError = null
                                regionError = null

                                var hasError = false

                                if (name.isBlank()) {
                                    nameError = "Debe introducir un nombre"
                                    hasError = true
                                }

                                if (username.isBlank()) {
                                    usernameError = "Debe introducir un nombre de usuario"
                                    hasError = true
                                } else if (username.length < 3) {
                                    usernameError = "Debe tener al menos 3 caracteres"
                                    hasError = true
                                }

                                if (selectedRegionId == null) {
                                    regionError = "Selecciona tu región / zona horaria"
                                    hasError = true
                                }

                                if (!isValidEmail(email)) {
                                    emailError = "El email no es válido"
                                    hasError = true
                                }

                                when {
                                    pass.length < 8 -> {
                                        passError =
                                            "La contraseña debe tener al menos 8 caracteres"
                                        hasError = true
                                    }

                                    !pass.any { it.isUpperCase() } -> {
                                        passError =
                                            "La contraseña debe incluir al menos una mayúscula"
                                        hasError = true
                                    }

                                    !pass.any { it.isDigit() } -> {
                                        passError =
                                            "La contraseña debe incluir al menos un número"
                                        hasError = true
                                    }
                                }

                                if (!hasError && selectedRegionId != null) {
                                    onRegisterClick(
                                        name,
                                        username,
                                        email,
                                        pass,
                                        selectedRegionId!!
                                    )
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
                                Text("Registrar")
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = onBackToLogin) {
                            Text("Volver al login")
                        }
                    }
                }
            }
        }
    }
}

fun isValidEmail(email: String): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()