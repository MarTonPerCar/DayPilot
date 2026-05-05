package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import com.example.daypilot_test_desing.ui.components.basic.MilestoneChip
import com.example.daypilot_test_desing.ui.components.basic.StepStatRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepsCard(
    currentSteps: Int,
    goalSteps: Int,
    pointsEarned: Int,
    pointsRemaining: Int,
    onConfigureGoal: (newGoal: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showGoalSheet by remember { mutableStateOf(false) }
    var sliderValue   by remember { mutableFloatStateOf(goalSteps.toFloat()) }
    val sheetState    = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val progress = (currentSteps.toFloat() / goalSteps.toFloat()).coerceIn(0f, 1f)
    val percentage = (progress * 100).toInt()

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label         = "steps_progress"
    )

    val milestone50  = currentSteps >= goalSteps * 0.5f
    val milestone75  = currentSteps >= goalSteps * 0.75f
    val milestone100 = currentSteps >= goalSteps

    val nextMilestone = when {
        !milestone50  -> "50%"
        !milestone75  -> "75%"
        !milestone100 -> "100%"
        else          -> "¡Meta cumplida!"
    }

    val primaryColor    = MaterialTheme.colorScheme.primary
    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant

    // ── BottomSheet ──────────────────────────────────────────────
    if (showGoalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGoalSheet = false },
            sheetState       = sheetState,
            shape            = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor   = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text       = "Configurar meta de pasos",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text       = "${sliderValue.toInt()} pasos",
                        style      = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
                Slider(
                    value         = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange    = 1000f..30000f,
                    steps         = 57,
                    colors        = SliderDefaults.colors(
                        thumbColor       = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("1.000", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("30.000", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    text  = "Metas rápidas",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(2000, 5000, 8000, 10000, 15000).forEach { preset ->
                        FilterChip(
                            selected = sliderValue.toInt() == preset,
                            onClick  = { sliderValue = preset.toFloat() },
                            label    = {
                                Text(
                                    text  = if (preset >= 1000) "${preset/1000}k" else "$preset",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            colors   = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor     = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = { showGoalSheet = false },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Cancelar") }
                    Button(
                        onClick = {
                            onConfigureGoal(sliderValue.toInt())
                            showGoalSheet = false
                        },
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(12.dp)
                    ) { Text("Guardar") }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }

    // ── Card ─────────────────────────────────────────────────────
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(24.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Cabecera con configurar meta ──────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.primary,
                        modifier           = Modifier.size(22.dp)
                    )
                    Text(
                        text       = "Pasos",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = { showGoalSheet = true }) {
                    Text(
                        text  = "Configurar meta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Circular + stats ──────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier         = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 14.dp.toPx()
                        val inset       = strokeWidth / 2
                        val arcSize     = Size(size.width - strokeWidth, size.height - strokeWidth)
                        val topLeft     = Offset(inset, inset)
                        drawArc(
                            color      = surfaceVarColor,
                            startAngle = 135f,
                            sweepAngle = 270f,
                            useCenter  = false,
                            topLeft    = topLeft,
                            size       = arcSize,
                            style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color      = primaryColor,
                            startAngle = 135f,
                            sweepAngle = 270f * animatedProgress,
                            useCenter  = false,
                            topLeft    = topLeft,
                            size       = arcSize,
                            style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text       = currentSteps.toString(),
                            style      = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color      = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text  = "de $goalSteps",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text       = "$percentage%",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(
                    modifier            = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text  = "Hitos",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MilestoneChip(label = "50%",  reached = milestone50)
                        MilestoneChip(label = "75%",  reached = milestone75)
                        MilestoneChip(label = "100%", reached = milestone100)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // ── Textos actualizados ───────────────────────
                    StepStatRow(
                        label = "Puntos ganados hoy",
                        value = "$pointsEarned pts"
                    )
                    StepStatRow(
                        label = "Siguiente meta",
                        value = nextMilestone
                    )
                }
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
                currentSteps    = 1200,
                goalSteps       = 2000,
                pointsEarned    = 1,
                pointsRemaining = 5,
                onConfigureGoal = {}
            )
        }
    }
}