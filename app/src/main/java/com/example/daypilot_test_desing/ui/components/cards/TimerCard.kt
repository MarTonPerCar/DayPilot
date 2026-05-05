package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import com.example.daypilot_test_desing.ui.model.TimerMode

@Composable
fun TimerCard(
    mode: TimerMode,
    pointEarnedToday: Boolean = false,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
    customMinutes: Int? = null
) {
    val duration = if (mode == TimerMode.CUSTOM && customMinutes != null)
        customMinutes else mode.durationMinutes

    val borderColor by animateColorAsState(
        targetValue = if (pointEarnedToday) MaterialTheme.colorScheme.primary
        else Color.Transparent,
        animationSpec = tween(400),
        label = "timer_border"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (pointEarnedToday) 1.5.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mode.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${duration} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Punto ganado
            if (pointEarnedToday) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Punto ganado hoy",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Botón iniciar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = onStart) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Iniciar",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TimerCard(mode = TimerMode.POMODORO,   pointEarnedToday = true,  onStart = {})
            TimerCard(mode = TimerMode.TRAINING,   pointEarnedToday = false, onStart = {})
            TimerCard(mode = TimerMode.MEDITATION, pointEarnedToday = false, onStart = {})
            TimerCard(mode = TimerMode.COOKING,    pointEarnedToday = false, onStart = {})
            TimerCard(mode = TimerMode.CUSTOM,     pointEarnedToday = false, onStart = {}, customMinutes = 45)
        }
    }
}