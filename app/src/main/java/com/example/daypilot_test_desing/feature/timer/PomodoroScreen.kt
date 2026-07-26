package com.example.daypilot_test_desing.feature.timer

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
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotTopBar
import kotlinx.coroutines.delay

private data class PomodoroPhase(
    val session: Int,
    val isWork: Boolean,
    val secondsLeft: Int,
    val isFinished: Boolean
)

private fun PomodoroPhase.advance(totalSessions: Int, workSeconds: Int, breakSeconds: Int): PomodoroPhase {
    if (isWork) return copy(isWork = false, secondsLeft = breakSeconds)
    return if (session < totalSessions) copy(session = session + 1, isWork = true, secondsLeft = workSeconds)
    else copy(isFinished = true)
}

/** All of [PomodoroScreen]'s ticking state, pulled into its own class purely so `runTicker()`'s
 *  loop is a real top-level member function instead of a local one nested inside the
 *  Composable — that's what actually keeps [PomodoroScreen]'s own cognitive complexity down. */
private class PomodoroState(private val totalSessions: Int, val workSeconds: Int, val breakSeconds: Int) {
    var currentSession by mutableIntStateOf(1)
    var isWorkPhase by mutableStateOf(true)
    var secondsLeft by mutableIntStateOf(workSeconds)
    var isRunning by mutableStateOf(false)
    var isFinished by mutableStateOf(false)
    var phaseEndCount by mutableIntStateOf(0)

    fun applyPhase(next: PomodoroPhase) {
        currentSession = next.session
        isWorkPhase = next.isWork
        secondsLeft = next.secondsLeft
        isFinished = next.isFinished
    }

    fun reset() {
        currentSession = 1
        isWorkPhase    = true
        secondsLeft    = workSeconds
        isRunning      = false
        isFinished     = false
    }

    fun skipPhase() {
        isRunning = false
        applyPhase(
            PomodoroPhase(currentSession, isWorkPhase, secondsLeft, isFinished)
                .advance(totalSessions, workSeconds, breakSeconds)
        )
    }

    suspend fun runTicker() {
        while (isRunning && !isFinished) {
            delay(1000)
            secondsLeft--

            if (secondsLeft <= 0) {
                isRunning = false
                phaseEndCount++
                applyPhase(
                    PomodoroPhase(currentSession, isWorkPhase, secondsLeft, isFinished)
                        .advance(totalSessions, workSeconds, breakSeconds)
                )
            }
        }
    }
}

@Composable
private fun rememberPomodoroState(totalSessions: Int, workSeconds: Int, breakSeconds: Int) =
    remember { PomodoroState(totalSessions, workSeconds, breakSeconds) }

@Composable
private fun PomodoroBody(
    state: PomodoroState,
    totalSessions: Int,
    arcColor: Color,
    workColor: Color,
    surfaceVarColor: Color,
    animatedProgress: Float
) {
    val minutes = state.secondsLeft / 60
    val seconds = state.secondsLeft % 60

    PomodoroSessionDots(
        totalSessions = totalSessions,
        currentSession = state.currentSession,
        workColor = workColor,
        arcColor = arcColor
    )

    PomodoroPhaseBadge(
        isWorkPhase = state.isWorkPhase,
        isFinished = state.isFinished,
        currentSession = state.currentSession,
        totalSessions = totalSessions,
        arcColor = arcColor
    )

    PomodoroRing(
        animatedProgress = animatedProgress,
        arcColor = arcColor,
        surfaceVarColor = surfaceVarColor,
        minutes = minutes,
        seconds = seconds,
        isWorkPhase = state.isWorkPhase
    )

    PomodoroControlsRow(
        isRunning = state.isRunning,
        isFinished = state.isFinished,
        arcColor = arcColor,
        onReset = { state.reset() },
        onToggle = { if (!state.isFinished) state.isRunning = !state.isRunning },
        onSkip = { if (!state.isFinished) state.skipPhase() }
    )

    if (state.isFinished) {
        PomodoroFinishedBadge()
    }
}

private suspend fun playPhaseSound(context: android.content.Context, isFinished: Boolean) {
    val uri = if (isFinished)
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
    else
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val ringtone = RingtoneManager.getRingtone(context, uri)
    ringtone?.play()
    delay(if (isFinished) 3_000L else 1_500L)
    if (ringtone?.isPlaying == true) ringtone.stop()
}

@Composable
private fun PomodoroSessionDots(totalSessions: Int, currentSession: Int, workColor: Color, arcColor: Color) {
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
}

@Composable
private fun PomodoroPhaseBadge(isWorkPhase: Boolean, isFinished: Boolean, currentSession: Int, totalSessions: Int, arcColor: Color) {
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
}

@Composable
private fun PomodoroRing(
    animatedProgress: Float,
    arcColor: Color,
    surfaceVarColor: Color,
    minutes: Int,
    seconds: Int,
    isWorkPhase: Boolean
) {
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
}

@Composable
private fun PomodoroResetButton(onReset: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onReset) {
            Icon(
                imageVector        = Icons.Default.Refresh,
                contentDescription = stringResource(R.string.pomodoro_reset),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PomodoroPlayPauseButton(isRunning: Boolean, isFinished: Boolean, arcColor: Color, onToggle: () -> Unit) {
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
            onClick = onToggle,
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
}

@Composable
private fun PomodoroSkipButton(arcColor: Color, onSkip: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(arcColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        IconButton(onClick = onSkip) {
            Icon(
                imageVector        = Icons.Default.SkipNext,
                contentDescription = stringResource(R.string.pomodoro_skip),
                tint               = arcColor,
                modifier           = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PomodoroControlsRow(
    isRunning: Boolean,
    isFinished: Boolean,
    arcColor: Color,
    onReset: () -> Unit,
    onToggle: () -> Unit,
    onSkip: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(20.dp),
        verticalAlignment     = Alignment.CenterVertically
    ) {
        PomodoroResetButton(onReset = onReset)
        PomodoroPlayPauseButton(isRunning = isRunning, isFinished = isFinished, arcColor = arcColor, onToggle = onToggle)
        PomodoroSkipButton(arcColor = arcColor, onSkip = onSkip)
    }
}

@Composable
private fun PomodoroFinishedBadge() {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    totalSessions: Int = 4,
    onCompleted: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state = rememberPomodoroState(totalSessions, workSeconds = 25 * 60, breakSeconds = 5 * 60)

    val totalSeconds = if (state.isWorkPhase) state.workSeconds else state.breakSeconds
    val progress = state.secondsLeft.toFloat() / totalSeconds

    val workColor  = Color(0xFFE53935)
    val breakColor = Color(0xFF1E88E5)
    val arcColor   = if (state.isWorkPhase) workColor else breakColor

    val animatedProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(500),
        label         = "pomodoro_progress"
    )

    val surfaceVarColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(state.isRunning) { state.runTicker() }

    LaunchedEffect(state.isFinished) {
        if (!state.isFinished) return@LaunchedEffect
        onCompleted()
    }

    LaunchedEffect(state.phaseEndCount) {
        if (state.phaseEndCount == 0) return@LaunchedEffect
        playPhaseSound(context, state.isFinished)
    }

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
            PomodoroBody(
                state = state,
                totalSessions = totalSessions,
                arcColor = arcColor,
                workColor = workColor,
                surfaceVarColor = surfaceVarColor,
                animatedProgress = animatedProgress
            )
        }
    }
}
