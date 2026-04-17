package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    currentSteps: Int,
    goalSteps: Int,
    pointsEarned: Int,
    pointsRemaining: Int,
    timerPointEarnedToday: Boolean,
    onBack: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToTechHealth: () -> Unit,
    onNavigateToTimer: (String) -> Unit,
    onConfigureStepsGoal: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Hábitos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Pasos ────────────────────────────────────────────
            StepsCard(
                currentSteps    = currentSteps,
                goalSteps       = goalSteps,
                pointsEarned    = pointsEarned,
                pointsRemaining = pointsRemaining,
                onConfigureGoal = onConfigureStepsGoal
            )

            // ── Cronómetros ──────────────────────────────────────
            Text(
                text = "Cronómetros",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            TimerMode.entries.forEach { mode ->
                TimerCard(
                    mode              = mode,
                    pointEarnedToday  = timerPointEarnedToday && mode == TimerMode.POMODORO,
                    onStart           = { onNavigateToTimer(mode.name) }
                )
            }

            // ── Otros hábitos ────────────────────────────────────
            Text(
                text = "Otros hábitos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HabitCard(
                title       = "Salud tecnológica",
                description = "Límites por app / grupo + avisos",
                icon        = Icons.Default.PhoneAndroid,
                onClick     = onNavigateToTechHealth
            )
            HabitCard(
                title       = "Recordatorios",
                description = "Avisos, timers y rutinas",
                icon        = Icons.Default.Notifications,
                onClick     = onNavigateToReminders
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}