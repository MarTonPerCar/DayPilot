package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class FilterOption<T>(
    val value: T,
    val label: String
)

@Composable
fun <T> DayPilotFilterSelector(
    selectedOption: FilterOption<T>,
    options: List<FilterOption<T>>,
    onSelect: (FilterOption<T>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape  = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = selectedOption.label,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector        = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                    tint               = MaterialTheme.colorScheme.primary
                )
            }
        }

        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text       = option.label,
                            fontWeight = if (option.value == selectedOption.value)
                                FontWeight.Bold else FontWeight.Normal,
                            color      = if (option.value == selectedOption.value)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                    trailingIcon = {
                        if (option.value == selectedOption.value) {
                            Icon(
                                imageVector        = Icons.Default.Check,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.primary,
                                modifier           = Modifier.size(14.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}