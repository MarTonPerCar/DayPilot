package com.example.daypilot_test_desing.ui.screens

import androidx.compose.ui.res.stringResource
import com.example.daypilot_test_desing.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBar
import com.example.daypilot_test_desing.ui.model.TimerMode
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    timerMode: String,
    customMinutes: Int = 0,
    pointEarnedToday: Boolean = false,
    onBack: () -> Unit
) {
    val mode         = TimerMode.entries.find { it.name == timerMode } ?: TimerMode.TRAINING
    val totalSeconds = if (customMinutes > 0) customMinutes * 60 else mode.durationMinutes * 60
    val label = if (timerMode == "CUSTOM") stringResource(R.string.timer_custom) else stringResource(mode.labelRes)

    var secondsLeft by remember { mutableIntStateOf(totalSeconds) }
    var isRunning   by remember { mutableStateOf(false) }
    var isFinished  by remember { mutableStateOf(false) }

    val progress = secondsLeft.toFloat() / totalSeconds

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(500),
        label         = "timer_progress"
    )

    LaunchedEffect(isRunning) {
        while (isRunning && secondsLeft > 0) {
            delay(1000)
            secondsLeft--
            if (secondsLeft == 0) {
                isRunning  = false
                isFinished = true
            }
        }
    }

    val minutes     = secondsLeft / 60
    val seconds     = secondsLeft % 60
    val primaryColor    = MaterialTheme.colorScheme.primary
    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = label,
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            // ── Círculo de progreso ──────────────────────────────
            Box(
                modifier         = Modifier.size(260.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val inset       = strokeWidth / 2
                    val arcSize     = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft     = Offset(inset, inset)

                    drawArc(
                        color      = surfaceVarColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color      = primaryColor,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter  = false,
                        topLeft    = topLeft,
                        size       = arcSize,
                        style      = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text       = "%02d:%02d".format(minutes, seconds),
                        fontSize   = 52.sp,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (customMinutes > 0) stringResource(R.string.timer_duration_minutes, customMinutes)
                        else stringResource(R.string.timer_duration_minutes, mode.durationMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isFinished) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.pomodoro_completed) + " 🎉",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // ── Punto ganado ─────────────────────────────────────
            if (pointEarnedToday || isFinished) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(text = "⭐", fontSize = 16.sp)
                    Text(
                        text = stringResource(R.string.timer_point_earned),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Controles ────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        secondsLeft = totalSeconds
                        isRunning   = false
                        isFinished  = false
                    }) {
                        Icon(
                            imageVector        = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.pomodoro_reset),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFinished) MaterialTheme.colorScheme.surfaceVariant
                            else MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick  = { if (!isFinished) isRunning = !isRunning },
                        enabled  = !isFinished
                    ) {
                        Icon(
                            imageVector        = if (isRunning) Icons.Default.Pause
                            else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) stringResource(R.string.pomodoro_pause) else stringResource(R.string.pomodoro_start),
                            tint               = if (isFinished)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else MaterialTheme.colorScheme.onPrimary,
                            modifier           = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}