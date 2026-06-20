package com.example.daypilot_test_desing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.example.daypilot_test_desing.R
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.data.model.CalendarTaskDot
import com.example.daypilot_test_desing.data.model.Month
import com.example.daypilot_test_desing.data.model.WeekDay
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import java.util.Calendar


@Composable
fun DayPilotCalendar(
    month: Int,
    year: Int,
    taskDots: List<CalendarTaskDot>,
    selectedDay: Int?,
    onDaySelected: (Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onAddTask: (day: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val daysInMonth  = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val startOffset  = (firstDayOfWeek + 5) % 7

    // Hoist today once — avoids 3 × Calendar.getInstance() per grid cell
    val nowCal      = Calendar.getInstance()
    val todayDay    = nowCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth  = nowCal.get(Calendar.MONTH) + 1
    val todayYear   = nowCal.get(Calendar.YEAR)

    // Pre-group dots by day so each cell lookup is O(1) instead of O(tasks)
    val dotsByDay   = taskDots.groupBy { it.day }

    val monthNames = Month.entries.map { stringResource(it.nameRes) }
    val dayHeaders = WeekDay.entries.map { stringResource(it.headerRes) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Cabecera mes ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = stringResource(R.string.calendar_prev_month),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${monthNames[month - 1]} $year",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = stringResource(R.string.calendar_next_month),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── Cabecera días semana ──────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { header ->
                    Text(
                        text = header,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Grid de días ─────────────────────────────────────
            val totalCells = startOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(rows) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val day = cellIndex - startOffset + 1

                            if (day < 1 || day > daysInMonth) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                val dots = dotsByDay[day]?.filter { it.month == month && it.year == year }.orEmpty()
                                val isSelected = day == selectedDay
                                val isToday = day == todayDay && month == todayMonth && year == todayYear

                                CalendarDayCell(
                                    day = day,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    dots = dots,
                                    onClick = { onDaySelected(day) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Celda de día ─────────────────────────────────────────────────
@Composable
fun CalendarDayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    dots: List<CalendarTaskDot>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )

        // Puntos de tareas
        if (dots.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                dots.take(3).forEach { dot ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else dot.color
                            )
                    )
                }
            }
        } else {
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DayPilotCalendarPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            DayPilotCalendar(
                month = 5,
                year = 2026,
                taskDots = listOf(
                    CalendarTaskDot(day = 5, month = 5, year = 2026, color = Color(0xFF4CAF50))
                ),
                selectedDay = 5,
                onDaySelected = {},
                onPreviousMonth = {},
                onNextMonth = {},
                onAddTask = {}
            )
        }
    }
}