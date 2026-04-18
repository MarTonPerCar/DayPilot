package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DayPilotDivider(
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 0.dp,
    thickness: Dp = 0.5.dp
) {
    HorizontalDivider(
        modifier  = modifier.padding(horizontal = horizontalPadding),
        thickness = thickness,
        color     = MaterialTheme.colorScheme.surfaceVariant
    )
}