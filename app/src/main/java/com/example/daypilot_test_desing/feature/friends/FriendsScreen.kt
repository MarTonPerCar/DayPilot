package com.example.daypilot_test_desing.feature.friends

import androidx.annotation.StringRes
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotEmptyState
import com.example.daypilot_test_desing.core.ui.components.basic.DayPilotTopBarWithAction
import com.example.daypilot_test_desing.core.ui.components.cards.FriendCard
import com.example.daypilot_test_desing.core.ui.components.cards.FriendRequestCard
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.ReactionType

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
    onReactToFriend: (userId: String, reaction: ReactionType) -> Unit = { _, _ -> },
    acceptingUserId: String? = null,
    justAcceptedRequest: Boolean = false,
    onAcceptedNavigated: () -> Unit = {},
    @StringRes userMessage: Int? = null,
    onMessageShown: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var friendToRemove by remember { mutableStateOf<FriendData?>(null) }
    val snackbarHost = remember { SnackbarHostState() }
    val messageText  = userMessage?.let { stringResource(it) }
    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbarHost.showSnackbar(messageText)
            onMessageShown()
        }
    }

    val tabs = listOf(
        stringResource(R.string.friends_tab_friends),
        stringResource(R.string.friends_tab_requests)
    )

    LaunchedEffect(justAcceptedRequest) {
        if (justAcceptedRequest) {
            selectedTab = 0
            onAcceptedNavigated()
        }
    }

    friendToRemove?.let { friend ->
        AlertDialog(
            onDismissRequest = { friendToRemove = null },
            title   = { Text(stringResource(R.string.friends_remove_confirm_title)) },
            text    = { Text(stringResource(R.string.friends_remove_confirm_message, friend.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onRemoveFriend(friend.id)
                    friendToRemove = null
                }) {
                    Text(
                        text  = stringResource(R.string.common_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { friendToRemove = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            DayPilotTopBarWithAction(
                title = stringResource(R.string.friends_title),
                actionIcon = Icons.Default.PersonAdd,
                actionDescription = stringResource(R.string.search_friends_title),
                onAction = onNavigateToSearch
            )
        },
        snackbarHost   = { SnackbarHost(hostState = snackbarHost) },
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
                                    onRemove = { friendToRemove = friend }
                                )
                            }
                        }
                    }
                }

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
                                    isAccepting = acceptingUserId == request.id,
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
