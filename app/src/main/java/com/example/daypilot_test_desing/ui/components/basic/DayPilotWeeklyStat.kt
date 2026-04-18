package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DayPilotWeeklyStat(
    emoji: String,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier            = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(text = emoji, fontSize = 16.sp)
        Text(
            text       = value,
            style      = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color      = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}