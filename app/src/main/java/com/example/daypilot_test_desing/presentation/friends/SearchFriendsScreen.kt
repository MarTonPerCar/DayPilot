package com.example.daypilot_test_desing.presentation.friends

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.UserSearchCard
import com.example.daypilot_test_desing.backend.model.SearchUserData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFriendsScreen(
    searchResults: List<SearchUserData>,
    isLoading: Boolean = false,
    requestJustSent: Boolean = false,
    onSearch: (query: String) -> Unit,
    onAddFriend: (userId: String) -> Unit,
    onConfirmationDismissed: () -> Unit = {},
    onBack: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    // Confirmation dialog after sending a request
    if (requestJustSent) {
        AlertDialog(
            onDismissRequest = {
                onConfirmationDismissed()
                onBack()
            },
            title = { Text(stringResource(R.string.user_request_sent_dialog_title)) },
            text  = { Text(stringResource(R.string.user_request_sent_dialog_message)) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirmationDismissed()
                    onBack()
                }) {
                    Text(stringResource(R.string.user_request_sent_dialog_button))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            DayPilotTopBar(
                title  = stringResource(R.string.search_friends_title),
                onBack = onBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value         = query,
                onValueChange = {
                    query = it
                    if (it.isNotEmpty()) onSearch(it)
                },
                placeholder = {
                    Text(
                        text  = stringResource(R.string.search_friends_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector        = Icons.Default.Search,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(visible = query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(
                                imageVector        = Icons.Default.Close,
                                contentDescription = stringResource(R.string.search_friends_clear),
                                tint               = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                singleLine = true,
                shape      = RoundedCornerShape(16.dp),
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor    = MaterialTheme.colorScheme.outline,
                    focusedContainerColor   = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor        = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor      = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            when {
                isLoading -> {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                query.isEmpty() -> {
                    DayPilotEmptyState(
                        message = stringResource(R.string.search_friends_empty_query),
                        icon    = Icons.Default.Search
                    )
                }

                searchResults.isEmpty() -> {
                    DayPilotEmptyState(
                        message = stringResource(R.string.search_friends_no_results, query),
                        icon    = Icons.Default.Search
                    )
                }

                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(searchResults) { user ->
                            UserSearchCard(
                                name               = user.name,
                                email              = user.email,
                                points             = user.points,
                                streak             = user.streak,
                                hasPendingRequest  = user.hasPendingRequest,
                                onAddFriend        = { onAddFriend(user.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
