package com.example.daypilot_test_desing.feature.friends

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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
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
import com.example.daypilot_test_desing.core.ui.components.cards.FriendCardInfo
import com.example.daypilot_test_desing.core.ui.components.cards.FriendRequestCard
import com.example.daypilot_test_desing.core.ui.components.cards.UserCardInfo
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.ReactionType

data class FriendsActions(
    val onAcceptRequest: (String) -> Unit,
    val onRejectRequest: (String) -> Unit,
    val onTapFriend: (String) -> Unit,
    val onRemoveFriend: (String) -> Unit,
    val onNavigateToSearch: () -> Unit,
    val onReactToFriend: (userId: String, reaction: ReactionType) -> Unit = { _, _ -> },
    val onAcceptedNavigated: () -> Unit = {},
    val onMessageShown: () -> Unit = {}
)

@Composable
private fun RemoveFriendDialog(friend: FriendData, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text(stringResource(R.string.friends_remove_confirm_title)) },
        text    = { Text(stringResource(R.string.friends_remove_confirm_message, friend.name)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text  = stringResource(R.string.common_delete),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

@Composable
private fun FriendsTabRow(
    tabs: List<String>,
    selectedTab: Int,
    friendRequestsCount: Int,
    onSelectTab: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.primary
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onSelectTab(index) },
                text = {
                    Text(
                        text = if (index == 1 && friendRequestsCount > 0)
                            "$title ($friendRequestsCount)"
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
}

@Composable
private fun FriendsListTab(
    friends: List<FriendData>,
    onReact: (FriendData, ReactionType) -> Unit,
    onRemoveRequested: (FriendData) -> Unit
) {
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
                    info = FriendCardInfo(
                        name = friend.name,
                        email = friend.email,
                        points = friend.points,
                        streak = friend.streak,
                        avatarUrl = friend.avatarUrl,
                        weeklySummary = friend.weeklySummary
                    ),
                    onReact = { reaction -> onReact(friend, reaction) },
                    onRemove = { onRemoveRequested(friend) }
                )
            }
        }
    }
}

@Composable
private fun FriendRequestsTab(
    friendRequests: List<FriendData>,
    acceptingUserId: String?,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit
) {
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
                    info = UserCardInfo(
                        name = request.name,
                        email = request.email,
                        points = request.points,
                        streak = request.streak
                    ),
                    isAccepting = acceptingUserId == request.id,
                    onAccept = { onAccept(request.id) },
                    onReject = { onReject(request.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    state: FriendsUiState,
    actions: FriendsActions
) {
    val friends             = state.friends
    val friendRequests      = state.friendRequests
    val acceptingUserId     = state.acceptingUserId
    val justAcceptedRequest = state.justAcceptedRequest
    val userMessage         = state.userMessage
    val onAcceptRequest      = actions.onAcceptRequest
    val onRejectRequest      = actions.onRejectRequest
    val onRemoveFriend       = actions.onRemoveFriend
    val onNavigateToSearch   = actions.onNavigateToSearch
    val onReactToFriend      = actions.onReactToFriend
    val onAcceptedNavigated  = actions.onAcceptedNavigated
    val onMessageShown       = actions.onMessageShown
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
        RemoveFriendDialog(
            friend = friend,
            onDismiss = { friendToRemove = null },
            onConfirm = {
                onRemoveFriend(friend.id)
                friendToRemove = null
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
            FriendsTabRow(
                tabs = tabs,
                selectedTab = selectedTab,
                friendRequestsCount = friendRequests.size,
                onSelectTab = { selectedTab = it }
            )

            when (selectedTab) {
                0 -> FriendsListTab(
                    friends = friends,
                    onReact = { friend, reaction -> onReactToFriend(friend.id, reaction) },
                    onRemoveRequested = { friendToRemove = it }
                )
                1 -> FriendRequestsTab(
                    friendRequests = friendRequests,
                    acceptingUserId = acceptingUserId,
                    onAccept = onAcceptRequest,
                    onReject = onRejectRequest
                )
            }
        }
    }
}
