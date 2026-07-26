package com.example.daypilot_test_desing.core.ui.components.cards

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Composable
private fun DeleteAppDialog(appName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(R.string.tech_health_delete_app_title),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(stringResource(R.string.tech_health_delete_app_message, appName))
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    stringResource(R.string.tech_health_delete_tomorrow),
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
private fun AppIconOrInitial(appIcon: Bitmap?, appName: String) {
    if (appIcon != null) {
        Image(
            bitmap             = appIcon.asImageBitmap(),
            contentDescription = null,
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = appName.first().uppercase(),
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun AppLimitHeaderRow(restriction: AppRestriction, appIcon: Bitmap?, onToggle: (Boolean) -> Unit) {
    val badgeDesc = stringResource(R.string.tech_health_app_badge_desc, restriction.appName)
    val switchDesc = stringResource(R.string.tech_health_switch_desc, restriction.appName)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AppIconOrInitial(appIcon = appIcon, appName = restriction.appName)

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = restriction.appName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .semantics {
                            contentDescription = badgeDesc
                        }
                ) {
                    Text(
                        text = stringResource(R.string.tech_health_app_badge),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = restriction.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Switch(
            checked = restriction.isEnabled,
            onCheckedChange = onToggle,
            enabled = !restriction.pendingDelete,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.semantics {
                contentDescription = switchDesc
            }
        )
    }
}

@Composable
private fun PendingStatusTexts(restriction: AppRestriction) {
    // Unlike pendingDelete, a pending toggle/limit change doesn't lock the card.
    if (restriction.pendingDelete) return
    if (restriction.pendingActive != null) {
        Text(
            text = stringResource(
                if (restriction.pendingActive) R.string.tech_health_pending_activate
                else R.string.tech_health_pending_deactivate
            ),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
    if (restriction.pendingLimitMinutes != null) {
        Text(
            text = stringResource(R.string.tech_health_pending_limit, restriction.pendingLimitMinutes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Composable
private fun UsageProgressSection(restriction: AppRestriction, isOverLimit: Boolean, progress: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isOverLimit) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Text(
            text = stringResource(
                R.string.tech_health_usage_today,
                restriction.usedMinutesToday,
                restriction.dailyLimitMinutes
            ),
            style = MaterialTheme.typography.labelSmall,
            color = if (isOverLimit) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AppLimitActionsRow(restriction: AppRestriction, onEdit: () -> Unit, onDeleteRequest: () -> Unit) {
    val editDesc = stringResource(R.string.tech_health_edit_desc, restriction.appName)
    val deleteDesc = stringResource(R.string.tech_health_delete_desc, restriction.appName)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (restriction.pendingDelete) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.tech_health_pending_delete),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else {
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = editDesc
                    },
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    stringResource(R.string.common_edit),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            TextButton(
                onClick = onDeleteRequest,
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = deleteDesc
                    }
            ) {
                Text(
                    text = stringResource(R.string.common_delete),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AppLimitCard(
    restriction: AppRestriction,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val appIcon: Bitmap? = remember(restriction.packageName) {
        try { context.packageManager.getApplicationIcon(restriction.packageName).toBitmap() }
        catch (_: Exception) { null }
    }

    val progress = (restriction.usedMinutesToday.toFloat() /
            restriction.dailyLimitMinutes).coerceIn(0f, 1f)
    val isOverLimit = restriction.usedMinutesToday >= restriction.dailyLimitMinutes

    if (showDeleteConfirm) {
        DeleteAppDialog(
            appName = restriction.appName,
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppLimitHeaderRow(restriction = restriction, appIcon = appIcon, onToggle = onToggle)

            PendingStatusTexts(restriction = restriction)

            UsageProgressSection(restriction = restriction, isOverLimit = isOverLimit, progress = progress)

            AppLimitActionsRow(
                restriction = restriction,
                onEdit = onEdit,
                onDeleteRequest = { showDeleteConfirm = true }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppLimitCardPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppLimitCard(
                restriction = AppRestriction(
                    id = "1",
                    appName = "YouTube",
                    packageName = "com.google.youtube",
                    dailyLimitMinutes = 120,
                    isEnabled = true,
                    usedMinutesToday = 45
                ),
                onToggle = {},
                onEdit = {},
                onDelete = {}
            )
            AppLimitCard(
                restriction = AppRestriction(
                    id = "2",
                    appName = "Instagram",
                    packageName = "com.instagram.android",
                    dailyLimitMinutes = 30,
                    isEnabled = true,
                    usedMinutesToday = 30,
                    pendingDelete = true
                ),
                onToggle = {},
                onEdit = {},
                onDelete = {}
            )
        }
    }
}
