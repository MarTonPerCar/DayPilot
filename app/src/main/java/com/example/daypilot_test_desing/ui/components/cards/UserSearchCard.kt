package com.example.daypilot_test_desing.ui.components.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotAvatar
import com.example.daypilot_test_desing.ui.components.basic.DayPilotIconButton
import com.example.daypilot_test_desing.ui.components.basic.DayPilotStatsRow
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── Base compartida ──────────────────────────────────────────────
@Composable
private fun UserCardBase(
    name: String,
    email: String,
    points: Int,
    streak: Int,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null,
    onClick: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DayPilotAvatar(name = name, avatarUrl = avatarUrl)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                DayPilotStatsRow(points = points, streak = streak)
            }

            action?.invoke()
        }
    }
}

// ── 1. UserSearchCard ─────────────────────────────────────────────
@Composable
fun UserSearchCard(
    name: String,
    email: String,
    points: Int,
    streak: Int,
    onAddFriend: () -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null
) {
    UserCardBase(
        name = name,
        email = email,
        points = points,
        streak = streak,
        avatarUrl = avatarUrl,
        modifier = modifier,
        action = {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                DayPilotIconButton(
                    icon = Icons.Default.Add,
                    onClick = onAddFriend,
                    contentDescription = stringResource(R.string.user_add_friend),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    )
}

// ── 2. FriendRequestCard ──────────────────────────────────────────
@Composable
fun FriendRequestCard(
    name: String,
    email: String,
    points: Int,
    streak: Int,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    avatarUrl: String? = null
) {
    UserCardBase(
        name = name,
        email = email,
        points = points,
        streak = streak,
        avatarUrl = avatarUrl,
        modifier = modifier,
        action = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    DayPilotIconButton(
                        icon = Icons.Default.Check,
                        onClick = onAccept,
                        contentDescription = stringResource(R.string.user_accept_request),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error),
                    contentAlignment = Alignment.Center
                ) {
                    DayPilotIconButton(
                        icon = Icons.Default.Close,
                        onClick = onReject,
                        contentDescription = stringResource(R.string.user_reject_request),
                        tint = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    )
}

// ── Preview ──────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun UserCardsPreview() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UserSearchCard(
                name = "Mario García",
                email = "mario@example.com",
                points = 340,
                streak = 7,
                onAddFriend = {}
            )
            FriendRequestCard(
                name = "Ana López",
                email = "ana@example.com",
                points = 210,
                streak = 3,
                onAccept = {},
                onReject = {}
            )
        }
    }
}