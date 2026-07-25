package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.MilestoneChip
import com.example.daypilot_test_desing.core.ui.components.basic.StepStatRow
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Immutable
data class StepsCardInfo(
    val currentSteps: Int,
    val goalSteps: Int,
    val pointsEarned: Int,
    val pointsRemaining: Int,
    val goalLocked: Boolean = false,
    val pendingGoal: Int? = null
)

@Composable
private fun GoalConfigSheetContent(
    sliderValue: Float,
    onSliderChange: (Float) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = stringResource(R.string.steps_goal_sheet_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.steps_goal_value, sliderValue.toInt()),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = sliderValue,
            onValueChange = onSliderChange,
            valueRange = 1000f..30000f,
            steps = 57,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "1.000",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "30.000",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = stringResource(R.string.steps_quick_goals),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(2000, 5000, 8000, 10000, 15000).forEach { preset ->
                FilterChip(
                    selected = sliderValue.toInt() == preset,
                    onClick = { onSliderChange(preset.toFloat()) },
                    label = {
                        Text(
                            text = if (preset >= 1000) "${preset / 1000}k" else "$preset",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(R.string.common_cancel)) }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) { Text(stringResource(R.string.common_save)) }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun StepsProgressRing(
    currentSteps: Int,
    goalSteps: Int,
    percentage: Int,
    animatedProgress: Float,
    primaryColor: Color,
    surfaceVarColor: Color
) {
    Box(
        modifier = Modifier.size(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val inset = strokeWidth / 2
            val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
            val topLeft = Offset(inset, inset)
            drawArc(
                color = surfaceVarColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = primaryColor,
                startAngle = 135f,
                sweepAngle = 270f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = currentSteps.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.steps_goal_of, goalSteps),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun PendingGoalBanner(pendingGoal: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.steps_pending_goal, pendingGoal),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsCard(
    info: StepsCardInfo,
    onConfigureGoal: (newGoal: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSteps    = info.currentSteps
    val goalSteps        = info.goalSteps
    val pointsEarned      = info.pointsEarned
    val pointsRemaining   = info.pointsRemaining
    val goalLocked        = info.goalLocked
    val pendingGoal       = info.pendingGoal
    var showGoalSheet by remember { mutableStateOf(false) }
    var sliderValue by remember(goalSteps) { mutableFloatStateOf(goalSteps.toFloat()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val progress = (currentSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "steps_progress"
    )

    val milestone50 = currentSteps >= goalSteps * 0.5f
    val milestone75 = currentSteps >= goalSteps * 0.75f
    val milestone100 = currentSteps >= goalSteps

    val nextMilestone = when {
        !milestone50 -> "50%"
        !milestone75 -> "75%"
        !milestone100 -> "100%"
        else -> stringResource(R.string.steps_goal_reached)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant

    if (showGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoalSheet = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            GoalConfigSheetContent(
                sliderValue = sliderValue,
                onSliderChange = { sliderValue = it },
                onCancel = { showGoalSheet = false },
                onSave = {
                    onConfigureGoal(sliderValue.toInt())
                    showGoalSheet = false
                }
            )
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    val stepsWidgetDesc = stringResource(R.string.steps_widget_desc)
                    Text(
                        text = stringResource(R.string.steps_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.semantics { contentDescription = stepsWidgetDesc }
                    )
                }
                TextButton(
                    onClick  = { if (!goalLocked) showGoalSheet = true },
                    enabled  = !goalLocked
                ) {
                    Text(
                        text  = if (goalLocked) stringResource(R.string.steps_goal_locked)
                                else stringResource(R.string.steps_configure_goal),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (goalLocked) MaterialTheme.colorScheme.onSurfaceVariant
                                else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StepsProgressRing(
                    currentSteps = currentSteps,
                    goalSteps = goalSteps,
                    percentage = percentage,
                    animatedProgress = animatedProgress,
                    primaryColor = primaryColor,
                    surfaceVarColor = surfaceVarColor
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.steps_milestones),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MilestoneChip(label = "50%", reached = milestone50)
                        MilestoneChip(label = "75%", reached = milestone75)
                        MilestoneChip(label = "100%", reached = milestone100)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    StepStatRow(
                        label = stringResource(R.string.steps_points_earned),
                        value = stringResource(R.string.steps_pts_value, pointsEarned)
                    )
                    StepStatRow(
                        label = stringResource(R.string.steps_next_milestone),
                        value = nextMilestone
                    )
                }
            }

            if (pendingGoal != null) {
                PendingGoalBanner(pendingGoal = pendingGoal)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StepsCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            StepsCard(
                info = StepsCardInfo(
                    currentSteps = 1200,
                    goalSteps = 2000,
                    pointsEarned = 1,
                    pointsRemaining = 5
                ),
                onConfigureGoal = {}
            )
        }
    }
}