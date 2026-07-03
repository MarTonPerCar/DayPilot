package com.example.daypilot_test_desing.feature.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.core.ui.components.cards.*
import com.example.daypilot_test_desing.core.data.model.DayProgress
import com.example.daypilot_test_desing.core.data.model.ProgressFilter

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
    var selectedFilter by remember { mutableStateOf(ProgressFilter.POINTS) }

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

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ProgressFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ProgressFilter.entries.size
                        ),
                        onClick = { selectedFilter = filter },
                        selected = filter == selectedFilter,
                        label = { Text(stringResource(filter.labelRes)) }
                    )
                }
            }

            ProgressChartCard(
                data   = progressData,
                filter = selectedFilter
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}