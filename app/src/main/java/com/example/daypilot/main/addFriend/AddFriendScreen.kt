package com.example.daypilot.main.addFriend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.firebaseLogic.authLogic.FriendRequest
import com.example.daypilot.firebaseLogic.authLogic.SearchUserResult
import com.example.daypilot.firebaseLogic.authLogic.UserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFriendScreen(
    authRepo: AuthRepository,
    currentUid: String,
    onBack: () -> Unit
) {

    // ========== State ==========

    val scope = rememberCoroutineScope()

    var currentProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<SearchUserResult>>(emptyList()) }
    var friendsSet by remember { mutableStateOf<Set<String>>(emptySet()) }

    var incomingRequests by remember { mutableStateOf<List<FriendRequest>>(emptyList()) }
    var isLoadingInvites by remember { mutableStateOf(false) }

    var infoMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // ========== Effects ==========

    LaunchedEffect(currentUid) {
        try {
            currentProfile = authRepo.getUserProfile(currentUid)
            friendsSet = authRepo.getFriends(currentUid).map { it.uid }.toSet()

            isLoadingInvites = true
            incomingRequests = authRepo.getIncomingFriendRequests(currentUid)
        } catch (_: Exception) {
            errorMessage = "Error cargando datos de amigos."
        } finally {
            isLoadingInvites = false
        }
    }

    LaunchedEffect(query) {
        errorMessage = null
        infoMessage = null

        val clean = query.trim()
        if (clean.isBlank()) {
            searchResults = emptyList()
            isSearching = false
            return@LaunchedEffect
        }

        isSearching = true
        delay(350)

        try {
            val results = authRepo.searchUsersByUsernamePrefix(clean)
                .filter { it.uid != currentUid }

            searchResults = results
            if (results.isEmpty()) infoMessage = "No se han encontrado usuarios."
        } catch (_: Exception) {
            errorMessage = "Error al buscar usuarios."
        } finally {
            isSearching = false
        }
    }

    // ========== UI ==========

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Amigos") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Buscar amigos") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Invitaciones") }
                )
            }

            when (selectedTabIndex) {
                0 -> SearchTab(
                    query = query,
                    onQueryChange = { query = it },
                    isSearching = isSearching,
                    searchResults = searchResults,
                    friendsSet = friendsSet,
                    currentUid = currentUid,
                    errorMessage = errorMessage,
                    infoMessage = infoMessage,
                    onSendRequest = { toUid, toUsername ->
                        val me = currentProfile
                        if (me == null) {
                            errorMessage = "Error: no se ha podido cargar tu perfil."
                            return@SearchTab
                        }

                        scope.launch {
                            try {
                                authRepo.sendFriendRequest(
                                    fromUid = currentUid,
                                    toUid = toUid,
                                    fromUsername = me.username,
                                    fromName = me.name
                                )
                                infoMessage = "Solicitud enviada a @$toUsername."
                            } catch (_: Exception) {
                                errorMessage = "Error al enviar la solicitud."
                            }
                        }
                    }
                )

                1 -> InvitationsTab(
                    isLoading = isLoadingInvites,
                    incomingRequests = incomingRequests,
                    onAccept = { request ->
                        scope.launch {
                            try {
                                authRepo.acceptFriendRequest(currentUid, request)
                                incomingRequests = incomingRequests.filterNot { it.fromUid == request.fromUid }
                                friendsSet = authRepo.getFriends(currentUid).map { it.uid }.toSet()
                            } catch (_: Exception) {
                                errorMessage = "Error al aceptar la invitación."
                            }
                        }
                    },
                    onDecline = { request ->
                        scope.launch {
                            try {
                                authRepo.declineFriendRequest(currentUid, request.fromUid)
                                incomingRequests = incomingRequests.filterNot { it.fromUid == request.fromUid }
                            } catch (_: Exception) {
                                errorMessage = "Error al rechazar la invitación."
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SearchTab(
    query: String,
    onQueryChange: (String) -> Unit,
    isSearching: Boolean,
    searchResults: List<SearchUserResult>,
    friendsSet: Set<String>,
    currentUid: String,
    errorMessage: String?,
    infoMessage: String?,
    onSendRequest: (toUid: String, toUsername: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Buscar por username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (infoMessage != null) {
            Text(
                text = infoMessage,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(searchResults) { result ->
                val profile = result.profile
                val isAlreadyFriend = friendsSet.contains(result.uid)

                AddFriendResultItem(
                    username = profile.username,
                    name = profile.name,
                    region = profile.region,
                    isAlreadyFriend = isAlreadyFriend,
                    isRequestMode = true,
                    onAction = {
                        if (isAlreadyFriend || result.uid == currentUid) return@AddFriendResultItem
                        onSendRequest(result.uid, profile.username)
                    }
                )
            }
        }
    }
}

@Composable
private fun InvitationsTab(
    isLoading: Boolean,
    incomingRequests: List<FriendRequest>,
    onAccept: (FriendRequest) -> Unit,
    onDecline: (FriendRequest) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            incomingRequests.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No tienes invitaciones pendientes.")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(incomingRequests) { request ->
                        InvitationItem(
                            request = request,
                            onAccept = { onAccept(request) },
                            onDecline = { onDecline(request) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddFriendResultItem(
    username: String,
    name: String,
    region: String,
    isAlreadyFriend: Boolean,
    isRequestMode: Boolean,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isAlreadyFriend && isRequestMode, onClick = onAction),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (name.takeIf { it.isNotBlank() } ?: username)
                        .first()
                        .uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name.ifBlank { "Usuario" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (region.isNotBlank()) {
                    Text(
                        text = region,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            when {
                isAlreadyFriend -> {
                    Text(
                        text = "Amigos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                isRequestMode -> {
                    IconButton(onClick = onAction) {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = "Enviar solicitud"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InvitationItem(
    request: FriendRequest,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = request.fromName.ifBlank { "Usuario" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "@${request.fromUsername}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onAccept,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }
                OutlinedButton(
                    onClick = onDecline,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Rechazar")
                }
            }
        }
    }
}