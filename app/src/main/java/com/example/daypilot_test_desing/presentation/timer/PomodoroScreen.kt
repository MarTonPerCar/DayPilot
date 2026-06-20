package com.example.daypilot_test_desing.presentation.timer

import android.media.RingtoneManager
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    totalSessions: Int = 4,
    onBack: () -> Unit
) {
    val context      = LocalContext.current
    val workSeconds  = 25 * 60
    val breakSeconds = 5  * 60

    var currentSession by remember { mutableIntStateOf(1) }
    var isWorkPhase    by remember { mutableStateOf(true) }
    var secondsLeft    by remember { mutableIntStateOf(workSeconds) }
    var isRunning      by remember { mutableStateOf(false) }
    var isFinished     by remember { mutableStateOf(false) }
    var phaseEndCount  by remember { mutableIntStateOf(0) }

    val totalSeconds = if (isWorkPhase) workSeconds else breakSeconds
    val progress     = secondsLeft.toFloat() / totalSeconds

    val workColor  = Color(0xFFE53935)
    val breakColor = Color(0xFF1E88E5)
    val arcColor   = if (isWorkPhase) workColor else breakColor

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(500),
        label         = "pomodoro_progress"
    )

    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant

    // Ticker
    LaunchedEffect(isRunning) {
        while (isRunning && !isFinished) {
            delay(1000)
            secondsLeft--

            if (secondsLeft <= 0) {
                isRunning = false
                phaseEndCount++

                if (isWorkPhase) {
                    isWorkPhase = false
                    secondsLeft = breakSeconds
                } else {
                    if (currentSession < totalSessions) {
                        currentSession++
                        isWorkPhase = true
                        secondsLeft = workSeconds
                    } else {
                        isFinished = true
                    }
                }
            }
        }
    }

    // Sonido al cambio de fase
    LaunchedEffect(phaseEndCount) {
        if (phaseEndCount == 0) return@LaunchedEffect
        val uri = if (isFinished)
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        else
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone?.play()
        delay(if (isFinished) 3_000L else 1_500L)
        if (ringtone?.isPlaying == true) ringtone.stop()
    }

    val minutes = secondsLeft / 60
    val seconds = secondsLeft % 60

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title = stringResource(R.string.pomodoro_title),
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
            verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)
        ) {
            // ── Indicador de sesiones ────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                repeat(totalSessions) { index ->
                    val sessionIndex = index + 1
                    val isDone       = sessionIndex < currentSession
                    val isCurrent    = sessionIndex == currentSession

                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 12.dp else 8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isDone    -> workColor.copy(alpha = 0.5f)
                                    isCurrent -> arcColor
                                    else      -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                    )
                }
            }

            // ── Fase actual ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(arcColor.copy(alpha = 0.12f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text     = if (isWorkPhase) "🔴" else "🔵",
                    fontSize = 14.sp
                )
                Text(
                    text       = if (isFinished) stringResource(R.string.pomodoro_completed)
                    else if (isWorkPhase) stringResource(R.string.pomodoro_session_work, currentSession, totalSessions)
                    else stringResource(R.string.pomodoro_session_break, currentSession, totalSessions),
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = arcColor
                )
            }

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
                        color      = arcColor,
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
                        text = if (isWorkPhase) stringResource(R.string.pomodoro_work_label) else stringResource(R.string.pomodoro_break_label),
                        style = MaterialTheme.typography.bodyMedium,
                        color = arcColor
                    )
                }
            }

            // ── Controles ────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                // Reset
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        currentSession = 1
                        isWorkPhase    = true
                        secondsLeft    = workSeconds
                        isRunning      = false
                        isFinished     = false
                    }) {
                        Icon(
                            imageVector        = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.pomodoro_reset),
                            tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }

                // Play / Pause
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFinished) MaterialTheme.colorScheme.surfaceVariant
                            else arcColor
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { if (!isFinished) isRunning = !isRunning },
                        enabled = !isFinished
                    ) {
                        Icon(
                            imageVector        = if (isRunning) Icons.Default.Pause
                            else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) stringResource(R.string.pomodoro_pause) else stringResource(R.string.pomodoro_start),
                            tint               = if (isFinished)
                                MaterialTheme.colorScheme.onSurfaceVariant
                            else Color.White,
                            modifier           = Modifier.size(32.dp)
                        )
                    }
                }

                // Skip fase
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(arcColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        if (!isFinished) {
                            isRunning = false
                            if (isWorkPhase) {
                                isWorkPhase = false
                                secondsLeft = breakSeconds
                            } else {
                                if (currentSession < totalSessions) {
                                    currentSession++
                                    isWorkPhase = true
                                    secondsLeft = workSeconds
                                } else {
                                    isFinished = true
                                }
                            }
                        }
                    }) {
                        Icon(
                            imageVector        = Icons.Default.SkipNext,
                            contentDescription = stringResource(R.string.pomodoro_skip),
                            tint               = arcColor,
                            modifier           = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // ── Punto ganado ─────────────────────────────────────
            if (isFinished) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(text = "⭐", fontSize = 16.sp)
                    Text(
                        text = stringResource(R.string.pomodoro_all_done),
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}