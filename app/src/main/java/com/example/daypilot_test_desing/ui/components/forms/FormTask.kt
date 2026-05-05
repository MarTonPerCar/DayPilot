package com.example.daypilot_test_desing.ui.components.forms

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme
import com.example.daypilot_test_desing.ui.model.TaskCategory
import com.example.daypilot_test_desing.ui.model.TaskDifficulty

// ── Datos ────────────────────────────────────────────────────────

// ── Formulario principal ─────────────────────────────────────────
@Composable
fun TaskFormCard(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    isEditing: Boolean = false
) {
    var title       by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category    by remember { mutableStateOf(com.example.daypilot_test_desing.ui.model.TaskCategory.PERSONAL) }
    var difficulty  by remember { mutableStateOf(com.example.daypilot_test_desing.ui.model.TaskDifficulty.EASY) }
    var duration    by remember { mutableStateOf(30) }
    var reminder    by remember { mutableStateOf(false) }
    var recurring   by remember { mutableStateOf(false) }
    var recurrenceDays by remember { mutableStateOf(1) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cabecera
        Text(
            text = if (isEditing) "Editar tarea" else "Nueva tarea",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // ── Sección 1: Información ───────────────────────────────
        FormSection(title = "Información", icon = Icons.Default.Edit) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Descripción (opcional)") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = outlinedTextFieldColors()
            )
        }

        // ── Sección 2: Detalles ──────────────────────────────────
        FormSection(title = "Detalles", icon = Icons.Default.List) {
            // Categoría
            Text(
                text = "Categoría",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            CategorySelector(selected = category, onSelect = { category = it })

            Spacer(Modifier.height(4.dp))

            // Dificultad
            Text(
                text = "Dificultad",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DifficultySelector(selected = difficulty, onSelect = { difficulty = it })

            Spacer(Modifier.height(4.dp))

            // Duración
            Text(
                text = "Duración estimada",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            DurationSelector(value = duration, onValueChange = { duration = it })
        }

        // ── Sección 3: Recordatorio ──────────────────────────────
        FormSection(title = "Recordatorio", icon = Icons.Default.Notifications) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Activar recordatorio",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Recibirás una notificación",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = reminder,
                    onCheckedChange = { reminder = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // ── Sección 4: Recurrencia ───────────────────────────────
        FormSection(title = "Recurrencia", icon = Icons.Default.Refresh) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Tarea recurrente",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Se repite cada X días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = recurring,
                    onCheckedChange = { recurring = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            AnimatedVisibility(
                visible = recurring,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Repetir cada",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    DurationSelector(
                        value = recurrenceDays,
                        onValueChange = { recurrenceDays = it },
                        unit = "días",
                        min = 1,
                        step = 1
                    )
                }
            }
        }

        // ── Botones ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Cancelar")
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f).height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor   = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(if (isEditing) "Guardar" else "Crear tarea")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ── Sección genérica ─────────────────────────────────────────────
@Composable
fun FormSection(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else -90f,
        label = "arrow"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabecera de sección — pulsable para colapsar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotation)
                )
            }

            // Contenido colapsable
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(200)),
                exit  = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Column(
                    modifier = Modifier.padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    content = content
                )
            }
        }
    }
}

// ── Selector de categoría ────────────────────────────────────────
@Composable
fun CategorySelector(
    selected: com.example.daypilot_test_desing.ui.model.TaskCategory,
    onSelect: (com.example.daypilot_test_desing.ui.model.TaskCategory) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        com.example.daypilot_test_desing.ui.model.TaskCategory.entries.forEach { cat ->
            val isSelected = cat == selected
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(cat) },
                label = { Text(cat.label, style = MaterialTheme.typography.labelMedium) },
                leadingIcon = {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

// ── Selector de dificultad ───────────────────────────────────────
@Composable
fun DifficultySelector(
    selected: com.example.daypilot_test_desing.ui.model.TaskDifficulty,
    onSelect: (com.example.daypilot_test_desing.ui.model.TaskDifficulty) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        com.example.daypilot_test_desing.ui.model.TaskDifficulty.entries.forEach { diff ->
            val isSelected = diff == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) diff.color
                        else diff.color.copy(alpha = 0.12f)
                    )
                    .border(
                        width = if (isSelected) 0.dp else 1.dp,
                        color = diff.color.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(diff) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = diff.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) Color.White else diff.color
                )
            }
        }
    }
}

// ── Selector de duración ─────────────────────────────────────────
@Composable
fun DurationSelector(
    value: Int,
    onValueChange: (Int) -> Unit,
    unit: String = "min",
    min: Int = 5,
    step: Int = 5
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedIconButton(
            onClick = { if (value - step >= min) onValueChange(value - step) },
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(
                Icons.Default.Remove,
                contentDescription = "Menos",
                modifier = Modifier.size(16.dp)
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        OutlinedIconButton(
            onClick = { onValueChange(value + step) },
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(10.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Más",
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ── Colores de TextField ─────────────────────────────────────────
@Composable
fun outlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor      = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
    focusedContainerColor   = MaterialTheme.colorScheme.background,
    unfocusedContainerColor = MaterialTheme.colorScheme.background,
    focusedTextColor        = MaterialTheme.colorScheme.onBackground,
    unfocusedTextColor      = MaterialTheme.colorScheme.onBackground,
    focusedLabelColor       = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor     = MaterialTheme.colorScheme.onSurfaceVariant
)

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun TaskFormPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            TaskFormCard(onSave = {}, onCancel = {})
        }
    }
}