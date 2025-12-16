package com.example.daypilot.main.mainZone.task

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.taskLogic.TaskDifficulty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFiltersRow(
    sortOption: TaskSortOption,
    onSortChange: (TaskSortOption) -> Unit,
    difficultyFilter: TaskDifficulty?,
    onDifficultyFilterChange: (TaskDifficulty?) -> Unit,
    statusFilter: TaskStatusFilter,
    onStatusFilterChange: (TaskStatusFilter) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var sortExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = sortExpanded,
                onExpandedChange = { sortExpanded = !sortExpanded },
                modifier = Modifier.weight(1f)
            ) {
                val sortLabel = when (sortOption) {
                    TaskSortOption.DATE -> "Fecha creación"
                    TaskSortOption.NAME -> "Nombre"
                    TaskSortOption.DURATION -> "Duración"
                    TaskSortOption.DIFFICULTY -> "Dificultad"
                    TaskSortOption.NEXT_DATE -> "Más próximo"
                }

                OutlinedTextField(
                    value = sortLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Ordenar por") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = sortExpanded,
                    onDismissRequest = { sortExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Más próximo") },
                        onClick = {
                            onSortChange(TaskSortOption.NEXT_DATE)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Fecha creación") },
                        onClick = {
                            onSortChange(TaskSortOption.DATE)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Nombre") },
                        onClick = {
                            onSortChange(TaskSortOption.NAME)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Duración") },
                        onClick = {
                            onSortChange(TaskSortOption.DURATION)
                            sortExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Dificultad") },
                        onClick = {
                            onSortChange(TaskSortOption.DIFFICULTY)
                            sortExpanded = false
                        }
                    )
                }
            }

            var diffExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = diffExpanded,
                onExpandedChange = { diffExpanded = !diffExpanded },
                modifier = Modifier.weight(1f)
            ) {
                val diffLabel = when (difficultyFilter) {
                    null -> "Todas"
                    TaskDifficulty.EASY -> "Fácil"
                    TaskDifficulty.MEDIUM -> "Media"
                    TaskDifficulty.HARD -> "Difícil"
                }

                OutlinedTextField(
                    value = diffLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Dificultad") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = diffExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = diffExpanded,
                    onDismissRequest = { diffExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Todas") },
                        onClick = {
                            onDifficultyFilterChange(null)
                            diffExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Fácil") },
                        onClick = {
                            onDifficultyFilterChange(TaskDifficulty.EASY)
                            diffExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Media") },
                        onClick = {
                            onDifficultyFilterChange(TaskDifficulty.MEDIUM)
                            diffExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Difícil") },
                        onClick = {
                            onDifficultyFilterChange(TaskDifficulty.HARD)
                            diffExpanded = false
                        }
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Estado:", style = MaterialTheme.typography.bodyMedium)

            FilterChip(
                selected = statusFilter == TaskStatusFilter.ALL,
                onClick = { onStatusFilterChange(TaskStatusFilter.ALL) },
                label = { Text("Todas") }
            )
            FilterChip(
                selected = statusFilter == TaskStatusFilter.PENDING,
                onClick = { onStatusFilterChange(TaskStatusFilter.PENDING) },
                label = { Text("Pendientes") }
            )
            FilterChip(
                selected = statusFilter == TaskStatusFilter.COMPLETED,
                onClick = { onStatusFilterChange(TaskStatusFilter.COMPLETED) },
                label = { Text("Completadas") }
            )
        }
    }
}