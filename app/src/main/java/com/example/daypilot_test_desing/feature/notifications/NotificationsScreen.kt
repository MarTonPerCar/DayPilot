package com.example.daypilot_test_desing.feature.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.*
import com.example.daypilot_test_desing.core.ui.components.cards.*
import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationData>,
    onTapNotification: (String) -> Unit,
    onMarkAllAsRead: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf<NotificationType?>(null) }

    val filters = listOf(
        null                      to stringResource(R.string.notifications_filter_all),
        NotificationType.TASK        to stringResource(R.string.notifications_filter_tasks),
        NotificationType.SOCIAL      to stringResource(R.string.notifications_filter_social),
        NotificationType.STEPS       to stringResource(R.string.notifications_filter_steps),
        NotificationType.STREAK      to stringResource(R.string.notifications_filter_streak),
        NotificationType.REMINDER    to stringResource(R.string.notifications_filter_reminders),
        NotificationType.ACHIEVEMENT to stringResource(R.string.notifications_filter_achievements)
    )

    val filtered = if (selectedFilter == null) notifications
    else notifications.filter { it.type == selectedFilter }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.notifications_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Subtítulo ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text  = stringResource(R.string.notifications_title),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (unreadCount > 0) {
                    TextButton(onClick = onMarkAllAsRead) {
                        Text(
                            text  = stringResource(R.string.notifications_mark_all_read),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ── Filtros ──────────────────────────────────────────
            LazyRow(
                contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { (type, label) ->
                    val isSelected = selectedFilter == type
                    FilterChip(
                        selected = isSelected,
                        onClick  = { selectedFilter = type },
                        label    = { Text(label, style = MaterialTheme.typography.labelMedium) },
                        leadingIcon = if (type != null) ({
                            Icon(
                                imageVector        = type.icon,
                                contentDescription = null,
                                modifier           = Modifier.size(14.dp),
                                tint               = if (isSelected)
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                else
                                    type.color
                            )
                        }) else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor    = MaterialTheme.colorScheme.primary,
                            selectedLabelColor        = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor  = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            // ── Lista ────────────────────────────────────────────
            if (filtered.isEmpty()) {
                DayPilotEmptyState(
                    message = stringResource(R.string.notifications_empty),
                    icon    = Icons.Default.Notifications
                )
            } else {
                LazyColumn(
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filtered) { notification ->
                        NotificationCard(
                            title   = notification.title,
                            message = notification.message,
                            timeAgo = notification.timeAgo,
                            type    = notification.type,
                            isRead  = notification.isRead,
                            onClick = { onTapNotification(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

