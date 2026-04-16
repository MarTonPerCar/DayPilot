package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.basic.CategoryChip
import com.example.daypilot_test_desing.ui.components.basic.DifficultyChip
import com.example.daypilot_test_desing.ui.components.basic.DurationChip
import com.example.daypilot_test_desing.ui.components.basic.TaskCategory
import com.example.daypilot_test_desing.ui.components.basic.TaskDifficulty
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── 1. TaskCard ──────────────────────────────────────────────────
@Composable
fun TaskCard(
    title: String,
    category: TaskCategory,
    difficulty: TaskDifficulty,
    durationMinutes: Int,
    hasReminder: Boolean = false,
    isCompleted: Boolean = false,
    onToggleComplete: (Boolean) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "text_color"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (isCompleted) 0.6f else 1f,
        animationSpec = tween(300),
        label = "card_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = cardAlpha)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onToggleComplete,
                colors = CheckboxDefaults.colors(
                    checkedColor   = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough
                        else TextDecoration.None
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DifficultyChip(difficulty = difficulty)
                    CategoryChip(category = category)
                    DurationChip(minutes = durationMinutes)
                    if (hasReminder) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Recordatorio",
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ── 2. TaskMiniCard ──────────────────────────────────────────────
@Composable
fun TaskMiniCard(
    title: String,
    difficulty: TaskDifficulty,
    isCompleted: Boolean = false,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "mini_text_color"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(
                width = 1.dp,
                color = difficulty.color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onTap() }
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(difficulty.color)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(
                textDecoration = if (isCompleted) TextDecoration.LineThrough
                else TextDecoration.None
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (isCompleted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ── 3. TaskDayCard ───────────────────────────────────────────────
@Composable
fun TaskDayCard(
    title: String,
    category: TaskCategory,
    durationMinutes: Int,
    isCompleted: Boolean = false,
    onToggleComplete: (Boolean) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "day_text_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = onToggleComplete,
                colors = CheckboxDefaults.colors(
                    checkedColor   = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        textDecoration = if (isCompleted) TextDecoration.LineThrough
                        else TextDecoration.None
                    ),
                    fontWeight = FontWeight.Medium,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryChip(category = category)
                    DurationChip(minutes = durationMinutes)
                }
            }
        }
    }
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun TaskCardsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        var task1Done by remember { mutableStateOf(false) }
        var task2Done by remember { mutableStateOf(true) }
        var task3Done by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("TaskCard", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            TaskCard(
                title = "Terminar el TFG antes de las 18:00",
                category = TaskCategory.STUDY,
                difficulty = TaskDifficulty.HARD,
                durationMinutes = 120,
                hasReminder = true,
                isCompleted = task1Done,
                onToggleComplete = { task1Done = it },
                onTap = {}
            )
            TaskCard(
                title = "Salir a correr",
                category = TaskCategory.SPORT,
                difficulty = TaskDifficulty.EASY,
                durationMinutes = 45,
                isCompleted = task2Done,
                onToggleComplete = { task2Done = it },
                onTap = {}
            )
            Spacer(Modifier.height(4.dp))
            Text("TaskMiniCard", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            TaskMiniCard(
                title = "Reunión de equipo",
                difficulty = TaskDifficulty.MEDIUM,
                isCompleted = false,
                onTap = {}
            )
            TaskMiniCard(
                title = "Comprar comida",
                difficulty = TaskDifficulty.EASY,
                isCompleted = true,
                onTap = {}
            )
            Spacer(Modifier.height(4.dp))
            Text("TaskDayCard", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            TaskDayCard(
                title = "Preparar presentación",
                category = TaskCategory.WORK,
                durationMinutes = 60,
                isCompleted = task3Done,
                onToggleComplete = { task3Done = it },
                onTap = {}
            )
        }
    }
}