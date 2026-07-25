package com.example.daypilot_test_desing.core.ui.components.cards

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotAvatar
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotIconButton
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotStatsRow
import com.example.daypilot_test_desing.core.ui.theme.DayPilotTheme

@Immutable
data class UserCardInfo(
    val name: String,
    val email: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null
)

@Composable
private fun UserCardBase(
    info: UserCardInfo,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    action: @Composable (() -> Unit)? = null
) {
    val name      = info.name
    val email     = info.email
    val points    = info.points
    val streak    = info.streak
    val avatarUrl = info.avatarUrl
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

@Composable
fun UserSearchCard(
    info: UserCardInfo,
    onAddFriend: () -> Unit,
    modifier: Modifier = Modifier,
    hasPendingRequest: Boolean = false
) {
    UserCardBase(
        info = info,
        modifier = modifier,
        action = {
            if (hasPendingRequest) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                    ) {
                        DayPilotIconButton(
                            icon = Icons.Default.Check,
                            onClick = {},
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = stringResource(R.string.user_request_sent),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
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
        }
    )
}

@Composable
fun FriendRequestCard(
    info: UserCardInfo,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    modifier: Modifier = Modifier,
    isAccepting: Boolean = false
) {
    UserCardBase(
        info = info,
        modifier = modifier,
        action = {
            if (isAccepting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp
                )
            } else {
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
        }
    )
}

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
                info = UserCardInfo(name = "Mario García", email = "mario@example.com", points = 340, streak = 7),
                onAddFriend = {}
            )
            FriendRequestCard(
                info = UserCardInfo(name = "Ana López", email = "ana@example.com", points = 210, streak = 3),
                onAccept = {},
                onReject = {}
            )
        }
    }
}