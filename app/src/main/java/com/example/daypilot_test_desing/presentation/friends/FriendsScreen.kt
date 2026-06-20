package com.example.daypilot_test_desing.presentation.friends

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.DayPilotEmptyState
import com.example.daypilot_test_desing.ui.components.basic.DayPilotTopBarWithAction
import com.example.daypilot_test_desing.ui.components.cards.FriendCard
import com.example.daypilot_test_desing.ui.components.cards.FriendRequestCard
import com.example.daypilot_test_desing.data.model.FriendData
import com.example.daypilot_test_desing.data.model.ReactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    friends: List<FriendData>,
    friendRequests: List<FriendData>,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onTapFriend: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onReactToFriend: (userId: String, reaction: ReactionType) -> Unit = { _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.friends_tab_friends),
        stringResource(R.string.friends_tab_requests)
    )

    Scaffold(
        topBar = {
            DayPilotTopBarWithAction(
                title = stringResource(R.string.friends_title),
                actionIcon = Icons.Default.PersonAdd,
                actionDescription = stringResource(R.string.search_friends_title),
                onAction = onNavigateToSearch
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = if (index == 1 && friendRequests.isNotEmpty())
                                    "$title (${friendRequests.size})"
                                else title,
                                fontWeight = if (selectedTab == index)
                                    FontWeight.SemiBold
                                else
                                    FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                // ── Amigos ────────────────────────────────────────
                0 -> {
                    if (friends.isEmpty()) {
                        DayPilotEmptyState(
                            message = stringResource(R.string.friends_empty),
                            icon = Icons.Default.PersonAdd
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            friends.forEach { friend ->
                                FriendCard(
                                    name = friend.name,
                                    email = friend.email,
                                    points = friend.points,
                                    streak = friend.streak,
                                    avatarUrl = friend.avatarUrl,
                                    weeklySummary = friend.weeklySummary,
                                    onReact = { reaction -> onReactToFriend(friend.id, reaction) },
                                    onRemove = { onRemoveFriend(friend.id) }
                                )
                            }
                        }
                    }
                }

                // ── Solicitudes ───────────────────────────────────
                1 -> {
                    if (friendRequests.isEmpty()) {
                        DayPilotEmptyState(
                            message = stringResource(R.string.friends_requests_empty)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(friendRequests) { request ->
                                FriendRequestCard(
                                    name = request.name,
                                    email = request.email,
                                    points = request.points,
                                    streak = request.streak,
                                    onAccept = { onAcceptRequest(request.id) },
                                    onReject = { onRejectRequest(request.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}