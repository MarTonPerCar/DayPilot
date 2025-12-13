package com.example.daypilot.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.authLogic.UserProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(val profile: UserProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepo: AuthRepository,
    uid: String,
    accountCreationTimestamp: Long?,
    onOpenSettings: () -> Unit,   // 👈 nuevo
    onBack: () -> Unit
) {
    var uiState by remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }

    LaunchedEffect(uid) {
        try {
            val profile = authRepo.getUserProfile(uid)
            uiState = if (profile != null) {
                ProfileUiState.Loaded(profile)
            } else {
                ProfileUiState.Error("Perfil no encontrado")
            }
        } catch (e: Exception) {
            uiState = ProfileUiState.Error("Error cargando perfil")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Opciones"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProfileUiState.Loaded -> {
                    val profile = state.profile

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Foto de perfil",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                        }

                        // Nombre y email
                        Text(
                            text = profile.name.ifBlank { "Usuario" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Día de creación
                        val createdText = remember(profile.createdAt, accountCreationTimestamp) {
                            val millis = when {
                                profile.createdAt != 0L -> profile.createdAt
                                accountCreationTimestamp != null -> accountCreationTimestamp
                                else -> null
                            }
                            millis?.let { "Desde: " + it.toDateString() } ?: ""
                        }

                        if (createdText.isNotEmpty()) {
                            Text(
                                text = createdText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Stats
                        ProfileStatsCard(
                            totalPoints = profile.totalPoints,
                            stepsPoints = profile.pointsSteps,
                            wellnessPoints = profile.pointsWellness,
                            tasksPoints = profile.pointsTasks,
                            todaySteps = profile.todaySteps
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Opciones tipo lista (sin logout)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = true),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ProfileOptionItem(
                                label = "Información personal",
                                leading = { Icon(Icons.Default.Person, contentDescription = null) }
                            )
                            ProfileOptionItem(
                                label = "Progreso y puntos",
                                leading = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null) }
                            )
                            ProfileOptionItem(
                                label = "Opciones",
                                leading = { Icon(Icons.Filled.Settings, contentDescription = null) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileStatsCard(
    totalPoints: Long,
    stepsPoints: Long,
    wellnessPoints: Long,
    tasksPoints: Long,
    todaySteps: Long
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resumen de hoy",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    title = "Puntos totales",
                    value = totalPoints.toString()
                )
                StatItem(
                    title = "Pasos (hoy)",
                    value = todaySteps.toString()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Steps", stepsPoints.toString())
                StatItem("Wellness", wellnessPoints.toString())
                StatItem("Tareas", tasksPoints.toString())
            }
        }
    }
}

@Composable
private fun StatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileOptionItem(
    label: String,
    leading: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leading?.invoke()
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}