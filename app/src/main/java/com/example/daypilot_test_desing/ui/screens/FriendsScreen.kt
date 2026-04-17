package com.example.daypilot_test_desing.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.cards.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    friends: List<FriendData>,
    friendRequests: List<FriendData>,
    searchResults: List<FriendData>,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onAddFriend: (String) -> Unit,
    onTapFriend: (String) -> Unit,
    onNavigateToSearch: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Amigos", "Solicitudes")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Amigos",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Buscar amigos",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Tabs ─────────────────────────────────────────────
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = MaterialTheme.colorScheme.background,
                contentColor     = MaterialTheme.colorScheme.primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text = {
                            Text(
                                text = if (index == 1 && friendRequests.isNotEmpty())
                                    "$title (${friendRequests.size})"
                                else title,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (selectedTab) {
                // ── Lista de amigos ───────────────────────────────
                0 -> {
                    if (friends.isEmpty()) {
                        EmptyState(
                            message = "Aún no tienes amigos.\n¡Busca y añade amigos!"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(friends) { friend ->
                                FriendCard(
                                    name   = friend.name,
                                    email  = friend.email,
                                    points = friend.points,
                                    streak = friend.streak,
                                    onTap  = { onTapFriend(friend.id) }
                                )
                            }
                        }
                    }
                }

                // ── Solicitudes ───────────────────────────────────
                1 -> {
                    if (friendRequests.isEmpty()) {
                        EmptyState(message = "No tienes solicitudes pendientes")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(friendRequests) { request ->
                                FriendRequestCard(
                                    name     = request.name,
                                    email    = request.email,
                                    points   = request.points,
                                    streak   = request.streak,
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

// ── Modelo de datos ──────────────────────────────────────────────
data class FriendData(
    val id: String,
    val name: String,
    val email: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null
)

// ── Estado vacío ─────────────────────────────────────────────────
@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}