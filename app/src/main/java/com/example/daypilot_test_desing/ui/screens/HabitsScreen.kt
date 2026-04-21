package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    currentSteps: Int,
    goalSteps: Int,
    pointsEarned: Int,
    pointsRemaining: Int,
    onBack: () -> Unit,
    onNavigateToSteps: () -> Unit,
    onNavigateToTimer: () -> Unit,
    onNavigateToReminders: () -> Unit,
    onNavigateToTechHealth: () -> Unit
) {
    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.habits_title),
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Pasos ────────────────────────────────────────────
            DayPilotSectionHeader(title = stringResource(R.string.steps_title))

            StepsCard(
                currentSteps    = currentSteps,
                goalSteps       = goalSteps,
                pointsEarned    = pointsEarned,
                pointsRemaining = pointsRemaining,
                onConfigureGoal = {}
            )

            // ── Secciones ─────────────────────────────────────────
            DayPilotSectionHeader(title = stringResource(R.string.habits_other))

            HabitCard(
                title       = stringResource(R.string.habits_timers_title),
                description = stringResource(R.string.habits_timers_description),
                icon        = Icons.Default.Timer,
                onClick     = onNavigateToTimer
            )

            HabitCard(
                title       = stringResource(R.string.habits_tech_health_title),
                description = stringResource(R.string.habits_tech_health_description),
                icon        = Icons.Default.PhoneAndroid,
                onClick     = onNavigateToTechHealth
            )

            HabitCard(
                title       = stringResource(R.string.habits_reminders_title),
                description = stringResource(R.string.habits_reminders_description),
                icon        = Icons.Default.Notifications,
                onClick     = onNavigateToReminders
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}