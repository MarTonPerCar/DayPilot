package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    streak: Int,
    stepsToday: Int,
    stepsGoal: Int,
    tasksCompleted: Int,
    tasksTotal: Int,
    pointsToday: Int,
    rankingPosition: Int,
    onNavigateToCalendar: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToRivalry: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "DayPilot",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración",
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
            // ── Resumen diario ───────────────────────────────────
            DailySummaryCard(
                userName       = userName,
                streak         = streak,
                stepsToday     = stepsToday,
                stepsGoal      = stepsGoal,
                tasksCompleted = tasksCompleted,
                tasksTotal     = tasksTotal,
                pointsToday    = pointsToday,
                rankingPosition = rankingPosition
            )

            // ── Grid 2x2 ─────────────────────────────────────────
            Text(
                text = "Secciones",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeMenuCard(
                    section  = HomeSection.CALENDAR,
                    data     = HomeSectionData.Calendar(
                        pendingTasks   = tasksTotal - tasksCompleted,
                        completedTasks = tasksCompleted
                    ),
                    onClick  = onNavigateToCalendar,
                    modifier = Modifier.weight(1f)
                )
                HomeMenuCard(
                    section  = HomeSection.PROGRESS,
                    data     = HomeSectionData.Progress(
                        currentPoints = pointsToday,
                        goalPoints    = 20
                    ),
                    onClick  = onNavigateToProgress,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeMenuCard(
                    section  = HomeSection.HABITS,
                    data     = HomeSectionData.Habits(
                        stepsProgress = stepsToday.toFloat() / stepsGoal,
                        timerDone     = false
                    ),
                    onClick  = onNavigateToHabits,
                    modifier = Modifier.weight(1f)
                )
                HomeMenuCard(
                    section  = HomeSection.RIVALRY,
                    data     = HomeSectionData.Rivalry(
                        position      = rankingPosition,
                        totalFriends  = 5
                    ),
                    onClick  = onNavigateToRivalry,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}