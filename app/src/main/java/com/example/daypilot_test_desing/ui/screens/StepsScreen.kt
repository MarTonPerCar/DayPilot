package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsScreen(
    currentSteps: Int,
    goalSteps: Int,
    pointsEarned: Int,
    pointsRemaining: Int,
    totalSteps7Days: Int,
    bestDaySteps: Int,
    dailyAverage: Int,
    goalStreak: Int,
    onBack: () -> Unit,
    onConfigureGoal: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pasos",
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
            StepsCard(
                currentSteps    = currentSteps,
                goalSteps       = goalSteps,
                pointsEarned    = pointsEarned,
                pointsRemaining = pointsRemaining,
                onConfigureGoal = {}
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