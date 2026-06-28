package com.example.daypilot_test_desing.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.backend.model.DayProgress
import com.example.daypilot_test_desing.backend.model.HomeSection
import com.example.daypilot_test_desing.backend.model.HomeSectionData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    streak: Int,
    stepsToday: Int,
    stepsGoal: Int,
    tasksCompleted: Int,
    tasksTotal: Int,
    progressData: List<DayProgress>,
    pointsToday: Int,
    rankingPosition: Int,
    friendCount: Int,
    timerCompletedToday: Boolean,
    onNavigateToCalendar: () -> Unit,
    onNavigateToHabits: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToRivalry: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(top = 8.dp, bottom = 4.dp)
        ) {
            val totalHeight   = maxHeight
            val summaryHeight = totalHeight * 0.38f
            val gridHeight    = totalHeight - summaryHeight - 10.dp

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── Resumen diario ───────────────────────────────
                DailySummaryCard(
                    userName        = userName,
                    streak          = streak,
                    stepsToday      = stepsToday,
                    stepsGoal       = stepsGoal,
                    tasksCompleted  = tasksCompleted,
                    tasksTotal      = tasksTotal,
                    pointsToday     = pointsToday,
                    rankingPosition = rankingPosition,
                    modifier        = Modifier
                        .fillMaxWidth()
                        .height(summaryHeight)
                )

                // ── Grid 2x2 ─────────────────────────────────────
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .height(gridHeight),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeMenuCardInline(
                            section  = HomeSection.CALENDAR,
                            data     = HomeSectionData.Calendar(
                                pendingTasks   = tasksTotal - tasksCompleted,
                                completedTasks = tasksCompleted
                            ),
                            onClick  = onNavigateToCalendar,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        HomeMenuCardInline(
                            section  = HomeSection.PROGRESS,
                            data     = HomeSectionData.Progress(
                                data = progressData
                            ),
                            onClick  = onNavigateToProgress,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }

                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HomeMenuCardInline(
                            section  = HomeSection.HABITS,
                            data     = HomeSectionData.Habits(
                                stepsProgress = stepsToday.toFloat() / stepsGoal,
                                timerDone     = timerCompletedToday
                            ),
                            onClick  = onNavigateToHabits,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                        HomeMenuCardInline(
                            section  = HomeSection.RIVALRY,
                            data     = HomeSectionData.Rivalry(
                                position     = rankingPosition,
                                totalFriends = friendCount
                            ),
                            onClick  = onNavigateToRivalry,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                }
            }
        }
    }
}