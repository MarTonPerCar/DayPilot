package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.ui.components.cards.StepsCard
import com.example.daypilot_test_desing.ui.components.cards.StepsSummaryCard

@Composable
fun StepsScreen(
    currentSteps    : Int,
    goalSteps       : Int,
    pointsEarned    : Int,
    pointsRemaining : Int,
    totalSteps7Days : Int,
    bestDaySteps    : Int,
    dailyAverage    : Int,
    goalStreak      : Int,
    onBack          : () -> Unit,
    onConfigureGoal : (Int) -> Unit
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
            StepsCard(
                currentSteps    = currentSteps,
                goalSteps       = goalSteps,
                pointsEarned    = pointsEarned,
                pointsRemaining = pointsRemaining,
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
