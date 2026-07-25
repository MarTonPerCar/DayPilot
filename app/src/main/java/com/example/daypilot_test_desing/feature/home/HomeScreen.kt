package com.example.daypilot_test_desing.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.core.ui.components.cards.*
import com.example.daypilot_test_desing.core.data.model.DayProgress
import com.example.daypilot_test_desing.core.data.model.HomeSection
import com.example.daypilot_test_desing.core.data.model.HomeSectionData

data class HomeActions(
    val onNavigateToCalendar: () -> Unit,
    val onNavigateToHabits: () -> Unit,
    val onNavigateToProgress: () -> Unit,
    val onNavigateToRivalry: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    actions: HomeActions
) {
    val userName            = state.userName
    val streak              = state.streak
    val stepsToday          = state.stepsToday
    val stepsGoal           = state.stepsGoal
    val tasksCompleted      = state.tasksCompleted
    val tasksTotal          = state.tasksTotal
    val progressData        = state.progressData
    val pointsToday         = state.pointsToday
    val rankingPosition     = state.rankingPosition
    val friendCount         = state.friendCount
    val timerCompletedToday = state.timerCompletedToday
    val onNavigateToCalendar = actions.onNavigateToCalendar
    val onNavigateToHabits   = actions.onNavigateToHabits
    val onNavigateToProgress = actions.onNavigateToProgress
    val onNavigateToRivalry  = actions.onNavigateToRivalry
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
                DailySummaryCard(
                    info = DailySummaryInfo(
                        userName        = userName,
                        streak          = streak,
                        stepsToday      = stepsToday,
                        stepsGoal       = stepsGoal,
                        tasksCompleted  = tasksCompleted,
                        tasksTotal      = tasksTotal,
                        pointsToday     = pointsToday,
                        rankingPosition = rankingPosition
                    ),
                    modifier        = Modifier
                        .fillMaxWidth()
                        .height(summaryHeight)
                )

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
                                stepsProgress = if (stepsGoal > 0) stepsToday.toFloat() / stepsGoal else 0f,
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
                                totalFriends = if (friendCount > 0) friendCount + 1 else 0
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