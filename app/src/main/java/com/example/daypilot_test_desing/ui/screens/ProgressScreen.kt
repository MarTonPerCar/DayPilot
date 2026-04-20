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
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.ui.model.DayProgress
import com.example.daypilot_test_desing.ui.model.ProgressFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    progressData: List<DayProgress>,
    rankingPosition: Int,
    pointsToday: Int,
    pointsFromTasks: Int,
    pointsFromSteps: Int,
    pointsFromHabits: Int,
    pointsFromTimers: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.progress_title),
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
            StatsCard(
                rankingPosition  = rankingPosition,
                pointsToday      = pointsToday,
                pointsFromTasks  = pointsFromTasks,
                pointsFromSteps  = pointsFromSteps,
                pointsFromHabits = pointsFromHabits,
                pointsFromTimers = pointsFromTimers
            )

            ProgressChartCard(
                data   = progressData,
                filter = ProgressFilter.POINTS
            )

            ProgressChartCard(
                data   = progressData,
                filter = ProgressFilter.STEPS
            )

            ProgressChartCard(
                data   = progressData,
                filter = ProgressFilter.TASKS
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}