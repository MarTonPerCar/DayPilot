package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.daypilot_test_desing.ui.components.basic.TaskDot

@Composable
fun CalendarDayCard(
    day: Int,
    isToday: Boolean = false,
    isSelected: Boolean = false,
    hasEasyTask: Boolean = false,
    hasMediumTask: Boolean = false,
    hasHardTask: Boolean = false,
    isCurrentMonth: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday    -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            else       -> Color.Transparent
        },
        animationSpec = tween(200),
        label = "day_bg"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected        -> MaterialTheme.colorScheme.onPrimary
            !isCurrentMonth   -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            isToday           -> MaterialTheme.colorScheme.primary
            else              -> MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(200),
        label = "day_text"
    )

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(
                if (isToday && !isSelected)
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(10.dp)
                    )
                else Modifier
            )
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Número del día
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        // Puntos de tareas
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasEasyTask) {
                TaskDot(color = Color(0xFF4CAF50))
            }
            if (hasMediumTask) {
                TaskDot(color = Color(0xFFFF9800))
            }
            if (hasHardTask) {
                TaskDot(color = Color(0xFFF44336))
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
@Composable
fun CalendarDayCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fila de ejemplo del calendario
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CalendarDayCard(
                    day = 14,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CalendarDayCard(
                    day = 15,
                    isToday = true,
                    isSelected = false,
                    hasEasyTask = true,
                    hasMediumTask = true,
                    isCurrentMonth = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CalendarDayCard(
                    day = 16,
                    isToday = false,
                    isSelected = true,
                    hasHardTask = true,
                    isCurrentMonth = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CalendarDayCard(
                    day = 17,
                    isToday = false,
                    isSelected = false,
                    hasEasyTask = true,
                    hasMediumTask = true,
                    hasHardTask = true,
                    isCurrentMonth = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CalendarDayCard(
                    day = 18,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = false,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CalendarDayCard(
                    day = 19,
                    isToday = false,
                    isSelected = false,
                    isCurrentMonth = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                CalendarDayCard(
                    day = 20,
                    isToday = false,
                    isSelected = false,
                    hasEasyTask = true,
                    isCurrentMonth = true,
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}