package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String,
    username: String,
    email: String,
    memberSince: String,
    level: Int,
    totalPoints: Int,
    currentStreak: Int,
    longestStreak: Int,
    rankingPosition: Int,
    pointsToday: Int,
    pointsFromTasks: Int,
    pointsFromSteps: Int,
    pointsFromHabits: Int,
    pointsFromTimers: Int,
    avatarUrl: String? = null,
    weeklySummary: WeeklySummaryData,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            DayPilotTopBarWithAction(
                title             = stringResource(R.string.profile_title),
                actionIcon        = Icons.Default.Settings,
                actionDescription = stringResource(R.string.common_settings),
                onAction          = onNavigateToSettings
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
            ProfileStatsCard(
                name          = name,
                username      = username,
                level         = level,
                totalPoints   = totalPoints,
                currentStreak = currentStreak,
                longestStreak = longestStreak,
                avatarUrl     = avatarUrl
            )

            // ── Info básica ──────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_email),
                        value = email
                    )
                    DayPilotDivider()
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_since),
                        value = memberSince
                    )
                    DayPilotDivider()
                    ProfileInfoRow(
                        label = stringResource(R.string.profile_username),
                        value = "@$username"
                    )
                }
            }

            // ── Ranking ──────────────────────────────────────────
            StatsCard(
                rankingPosition  = rankingPosition,
                pointsToday      = pointsToday,
                pointsFromTasks  = pointsFromTasks,
                pointsFromSteps  = pointsFromSteps,
                pointsFromHabits = pointsFromHabits,
                pointsFromTimers = pointsFromTimers
            )

            Spacer(Modifier.height(8.dp))

            WeeklyReactionCard(summary = weeklySummary)
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = value,
            style      = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color      = MaterialTheme.colorScheme.onSurface
        )
    }
}