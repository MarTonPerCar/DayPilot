package com.example.daypilot_test_desing.feature.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.*
import com.example.daypilot_test_desing.core.ui.components.cards.*

data class HabitsActions(
    val onBack: () -> Unit,
    val onNavigateToTimer: () -> Unit,
    val onNavigateToReminders: () -> Unit,
    val onNavigateToTechHealth: () -> Unit,
    val onConfigureGoal: (Int) -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    state: HabitsUiState,
    actions: HabitsActions
) {
    val currentSteps     = state.currentSteps
    val goalSteps        = state.goalSteps
    val pointsEarned     = state.pointsEarned
    val pointsRemaining  = state.pointsRemaining
    val goalChangedToday = state.goalChangedToday
    val pendingGoal      = state.pendingGoal
    val onBack                 = actions.onBack
    val onNavigateToTimer      = actions.onNavigateToTimer
    val onNavigateToReminders  = actions.onNavigateToReminders
    val onNavigateToTechHealth = actions.onNavigateToTechHealth
    val onConfigureGoal        = actions.onConfigureGoal
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
            val stepsSectionDesc = stringResource(R.string.steps_section_desc)
            DayPilotSectionHeader(
                title = stringResource(R.string.steps_title),
                modifier = Modifier.semantics { contentDescription = stepsSectionDesc }
            )

            StepsCard(
                info = StepsCardInfo(
                    currentSteps    = currentSteps,
                    goalSteps       = goalSteps,
                    pointsEarned    = pointsEarned,
                    pointsRemaining = pointsRemaining,
                    goalLocked      = goalChangedToday,
                    pendingGoal     = pendingGoal
                ),
                onConfigureGoal = onConfigureGoal
            )

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