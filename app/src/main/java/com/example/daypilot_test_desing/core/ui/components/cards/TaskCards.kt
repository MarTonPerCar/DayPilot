package com.example.daypilot_test_desing.core.ui.components.cards

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.CategoryChip
import com.example.daypilot_test_desing.core.ui.components.basic.DifficultyChip
import com.example.daypilot_test_desing.core.ui.components.basic.DurationChip
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Immutable
data class TaskCardUiState(
    val title: String,
    val category: TaskCategory,
    val difficulty: TaskDifficulty,
    val durationMinutes: Int,
    val hasReminder: Boolean = false,
    val isCompleted: Boolean = false
)

@Composable
fun TaskCard(
    state: TaskCardUiState,
    onToggleComplete: (Boolean) -> Unit,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val title           = state.title
    val category        = state.category
    val difficulty      = state.difficulty
    val durationMinutes = state.durationMinutes
    val hasReminder     = state.hasReminder
    val isCompleted     = state.isCompleted
    val textColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "text_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTap() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                    checkedColor = MaterialTheme.colorScheme.primary,
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
                            contentDescription = stringResource(R.string.task_reminder_icon),
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskMiniCard(
    title: String,
    difficulty: TaskDifficulty,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
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

@Immutable
data class TaskDayCardUiState(
    val title: String,
    val category: TaskCategory,
    val difficulty: TaskDifficulty,
    val durationMinutes: Int,
    val isCompleted: Boolean = false,
    val hasReminder: Boolean = false,
    val isRecurring: Boolean = false,
    val isPending: Boolean = false
)

data class TaskDayCardActions(
    val onToggleComplete: (Boolean) -> Unit,
    val onTap: () -> Unit,
    val onEdit: () -> Unit,
    val onDelete: () -> Unit
)

@Composable
private fun DeleteTaskDialog(taskTitle: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.task_delete_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(stringResource(R.string.task_delete_message, taskTitle))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.common_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun TaskDifficultyCategoryStrip(difficultyColor: Color, categoryColor: Color, isCompleted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(3.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(difficultyColor.copy(alpha = if (isCompleted) 0.3f else 0.8f))
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(categoryColor.copy(alpha = if (isCompleted) 0.3f else 0.8f))
        )
    }
}

@Composable
private fun TaskDayBadgesRow(
    category: TaskCategory,
    durationMinutes: Int,
    hasReminder: Boolean,
    isRecurring: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CategoryChip(category = category)
        DurationChip(minutes = durationMinutes)
        if (hasReminder) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        if (isRecurring) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun TaskDayTrailingActions(isPending: Boolean, onEdit: () -> Unit, onDeleteRequest: () -> Unit) {
    if (isPending) {
        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.common_edit),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = onDeleteRequest,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun TaskDayCard(
    state: TaskDayCardUiState,
    actions: TaskDayCardActions,
    modifier: Modifier = Modifier
) {
    val title           = state.title
    val category        = state.category
    val difficulty      = state.difficulty
    val durationMinutes = state.durationMinutes
    val isCompleted     = state.isCompleted
    val hasReminder     = state.hasReminder
    val isRecurring     = state.isRecurring
    val isPending       = state.isPending
    val onToggleComplete = actions.onToggleComplete
    val onTap             = actions.onTap
    val onEdit            = actions.onEdit
    val onDelete          = actions.onDelete
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        DeleteTaskDialog(
            taskTitle = title,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }

    val textColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "day_text_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = !isPending) { onTap() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        TaskDifficultyCategoryStrip(
            difficultyColor = difficulty.color,
            categoryColor = category.color,
            isCompleted = isCompleted
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = if (isPending) null else onToggleComplete,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
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
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                TaskDayBadgesRow(
                    category = category,
                    durationMinutes = durationMinutes,
                    hasReminder = hasReminder,
                    isRecurring = isRecurring
                )
            }

            TaskDayTrailingActions(
                isPending = isPending,
                onEdit = onEdit,
                onDeleteRequest = { showDeleteConfirm = true }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCardsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskCard(
                state = TaskCardUiState(
                    title = "Terminar TFG",
                    category = TaskCategory.STUDY,
                    difficulty = TaskDifficulty.HARD,
                    durationMinutes = 120,
                    isCompleted = false
                ),
                onToggleComplete = {},
                onTap = {}
            )
            TaskMiniCard(
                title = "Reunión de equipo",
                difficulty = TaskDifficulty.MEDIUM,
                onTap = {}
            )
            TaskDayCard(
                state = TaskDayCardUiState(
                    title = "Presentación",
                    category = TaskCategory.WORK,
                    difficulty = TaskDifficulty.HARD,
                    durationMinutes = 60,
                    isCompleted = false
                ),
                actions = TaskDayCardActions(
                    onToggleComplete = {},
                    onTap = {},
                    onEdit = {},
                    onDelete = {}
                )
            )
        }
    }
}