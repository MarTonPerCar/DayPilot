package com.example.daypilot_test_desing.presentation.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.ui.components.cards.StepsCard
import com.example.daypilot_test_desing.ui.components.cards.StepsSummaryCard

@Composable
fun StepsScreen(
    currentSteps     : Int,
    goalSteps        : Int,
    pointsEarned     : Int,
    pointsRemaining  : Int,
    totalSteps7Days  : Int,
    bestDaySteps     : Int,
    dailyAverage     : Int,
    goalStreak       : Int,
    pendingGoal      : Int? = null,
    goalChangedToday : Boolean = false,
    sensorAvailable  : Boolean = true,
    onBack           : () -> Unit,
    onConfigureGoal  : (Int) -> Unit
) {
    Scaffold(
        topBar         = {
            DayPilotTopBar(
                title  = stringResource(R.string.steps_title),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!sensorAvailable) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = stringResource(R.string.steps_sensor_unavailable),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            StepsCard(
                currentSteps    = currentSteps,
                goalSteps       = goalSteps,
                pointsEarned    = pointsEarned,
                pointsRemaining = pointsRemaining,
                goalLocked      = goalChangedToday,
                pendingGoal     = pendingGoal,
                onConfigureGoal = onConfigureGoal
            )
            StepsSummaryCard(
                totalSteps7Days = totalSteps7Days,
                bestDaySteps    = bestDaySteps,
                dailyAverage    = dailyAverage,
                goalStreak      = goalStreak
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}
