package com.example.daypilot_test_desing.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import com.example.daypilot_test_desing.ui.model.CalendarTaskDot


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

    val daysInMonth   = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)

    // Convierte domingo=1..sábado=7 a lunes=0..domingo=6
    val startOffset = (firstDayOfWeek + 5) % 7

    val monthNames = listOf(
        "Enero", "Febrero", "Marzo", "Abril",
        "Mayo", "Junio", "Julio", "Agosto",
        "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )

    val dayHeaders = listOf("L", "M", "X", "J", "V", "S", "D")

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(
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
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector        = Icons.Default.ChevronLeft,
                        contentDescription = "Mes anterior",
                        tint               = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text       = "${monthNames[month - 1]} $year",
                    style      = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector        = Icons.Default.ChevronRight,
                        contentDescription = "Mes siguiente",
                        tint               = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // ── Cabecera días semana ──────────────────────────────
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { header ->
                    Text(
                        text      = header,
                        modifier  = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style     = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color     = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Grid de días ─────────────────────────────────────
            val totalCells = startOffset + daysInMonth
            val rows       = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(rows) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val cellIndex = row * 7 + col
                            val day       = cellIndex - startOffset + 1

                            if (day < 1 || day > daysInMonth) {
                                Box(modifier = Modifier.weight(1f))
                            } else {
                                val dots       = taskDots.filter { it.day == day }
                                val isSelected = day == selectedDay
                                val isToday    = day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH) &&
                                        month == Calendar.getInstance().get(Calendar.MONTH) + 1 &&
                                        year  == Calendar.getInstance().get(Calendar.YEAR)

                                CalendarDayCell(
                                    day        = day,
                                    isSelected = isSelected,
                                    isToday    = isToday,
                                    dots       = dots,
                                    onClick    = { onDaySelected(day) },
                                    modifier   = Modifier.weight(1f)
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
        modifier            = modifier
            .padding(2.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday    -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    else       -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text      = day.toString(),
            style     = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
            color     = when {
                isSelected -> MaterialTheme.colorScheme.onPrimary
                isToday    -> MaterialTheme.colorScheme.primary
                else       -> MaterialTheme.colorScheme.onSurface
            },
            textAlign = TextAlign.Center
        )

        // Puntos de tareas
        if (dots.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment     = Alignment.CenterVertically
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