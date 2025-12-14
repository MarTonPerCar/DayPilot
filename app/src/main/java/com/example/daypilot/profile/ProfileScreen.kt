package com.example.daypilot.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.daypilot.authLogic.AuthRepository
import com.example.daypilot.authLogic.FriendInfo
import com.example.daypilot.authLogic.UserProfile
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --------- ESTADO DE UI ---------

private sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Loaded(
        val profile: UserProfile,
        val friends: List<FriendInfo>
    ) : ProfileUiState()

    data class Error(val message: String) : ProfileUiState()
}

// --------- PROFILE SCREEN ---------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authRepo: AuthRepository,
    uid: String,
    accountCreationTimestamp: Long?,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit // lo dejamos por compatibilidad aunque no lo usemos
) {
    var uiState by remember { mutableStateOf<ProfileUiState>(ProfileUiState.Loading) }
    var showFriendsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    // estado del miniperfil de amigo
    var selectedFriend by remember { mutableStateOf<FriendInfo?>(null) }
    var selectedFriendProfile by remember { mutableStateOf<UserProfile?>(null) }
    var selectedFriendFriendsCount by remember { mutableStateOf(0) }
    var loadingFriend by remember { mutableStateOf(false) }

    fun reloadProfile() {
        scope.launch {
            uiState = ProfileUiState.Loading
            try {
                val profile = authRepo.getUserProfile(uid)
                if (profile != null) {
                    val friends = authRepo.getFriends(uid)
                    uiState = ProfileUiState.Loaded(profile, friends)
                } else {
                    uiState = ProfileUiState.Error("Perfil no encontrado")
                }
            } catch (e: Exception) {
                uiState = ProfileUiState.Error("Error cargando perfil")
            }
        }
    }

    // Carga inicial
    LaunchedEffect(uid) {
        reloadProfile()
    }

    // Recargar al volver de Settings (ON_RESUME)
    DisposableEffect(lifecycleOwner, uid) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                reloadProfile()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // ---- Bottom sheet de amigos / miniperfil ----
    if (showFriendsSheet) {
        val state = uiState
        if (state is ProfileUiState.Loaded) {
            ModalBottomSheet(
                onDismissRequest = {
                    showFriendsSheet = false
                    selectedFriend = null
                    selectedFriendProfile = null
                },
                sheetState = sheetState
            ) {
                when {
                    loadingFriend -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    selectedFriend != null && selectedFriendProfile != null -> {
                        FriendMiniProfileSheet(
                            friendProfile = selectedFriendProfile!!,
                            friendsCount = selectedFriendFriendsCount,
                            onBackToList = {
                                selectedFriend = null
                                selectedFriendProfile = null
                            },
                            onClose = {
                                scope.launch {
                                    sheetState.hide()
                                    showFriendsSheet = false
                                    selectedFriend = null
                                    selectedFriendProfile = null
                                }
                            }
                        )
                    }

                    else -> {
                        FriendsSheetList(
                            friends = state.friends,
                            onFriendClick = { friend ->
                                selectedFriend = friend
                                loadingFriend = true
                                scope.launch {
                                    try {
                                        val profile = authRepo.getUserProfile(friend.uid)
                                        val friendFriends = authRepo.getFriends(friend.uid)
                                        selectedFriendProfile = profile
                                        selectedFriendFriendsCount = friendFriends.size
                                    } finally {
                                        loadingFriend = false
                                    }
                                }
                            },
                            onClose = {
                                scope.launch {
                                    sheetState.hide()
                                    showFriendsSheet = false
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Opciones"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }

                is ProfileUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProfileUiState.Loaded -> {
                    val profile = state.profile
                    val friends = state.friends
                    val friendsCount = friends.size

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // --------- AVATAR ---------
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!profile.photoUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = profile.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Foto de perfil",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(56.dp)
                                )
                            }
                        }

                        // --------- NOMBRE ---------
                        Text(
                            text = profile.name.ifBlank { "Usuario" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        // --------- USERNAME ---------
                        if (profile.username.isNotBlank()) {
                            Text(
                                text = "@${profile.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // --------- EMAIL ---------
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // --------- DÍA DE CREACIÓN ---------
                        val createdText = remember(profile.createdAt, accountCreationTimestamp) {
                            val millis = when {
                                profile.createdAt != 0L -> profile.createdAt
                                accountCreationTimestamp != null -> accountCreationTimestamp
                                else -> null
                            }
                            millis?.let { "Desde: " + it.toDateString() } ?: ""
                        }

                        if (createdText.isNotEmpty()) {
                            Text(
                                text = createdText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // --------- TARJETA STATS ---------
                        ProfileStatsCard(
                            totalPoints = profile.totalPoints,
                            todaySteps = profile.todaySteps,
                            stepsPoints = profile.pointsSteps,
                            wellnessPoints = profile.pointsWellness,
                            tasksPoints = profile.pointsTasks,
                            friendsCount = friendsCount
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // --------- SECCIÓN AMIGOS (card que abre el sheet) ---------
                        FriendsSection(
                            friendsCount = friendsCount,
                            onOpenFriends = { showFriendsSheet = true }
                        )
                    }
                }
            }
        }
    }
}

// --------- TARJETA DE STATS (REUTILIZABLE) ---------

@Composable
private fun ProfileStatsCard(
    totalPoints: Long,
    todaySteps: Long,
    stepsPoints: Long,
    wellnessPoints: Long,
    tasksPoints: Long,
    friendsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Resumen",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem(
                    title = "Puntos totales",
                    value = totalPoints.toString()
                )
                StatItem(
                    title = "Pasos (hoy)",
                    value = todaySteps.toString()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("Steps", stepsPoints.toString())
                StatItem("Wellness", wellnessPoints.toString())
                StatItem("Tareas", tasksPoints.toString())
                StatItem("Amigos", friendsCount.toString())
            }
        }
    }
}

@Composable
private fun StatItem(title: String, value: String) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

// --------- CARD DE SECCIÓN AMIGOS ---------

@Composable
private fun FriendsSection(
    friendsCount: Int,
    onOpenFriends: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onOpenFriends)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Group,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Amigos",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$friendsCount amigos",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Ver",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// --------- LISTA AMIGOS EN EL SHEET ---------

@Composable
private fun FriendsSheetList(
    friends: List<FriendInfo>,
    onFriendClick: (FriendInfo) -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Amigos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${friends.size} en total",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (friends.isEmpty()) {
            Text(
                text = "Todavía no tienes amigos añadidos.",
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxHeight(0.8f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(friends) { friend ->
                    FriendRow(friend = friend, onClick = { onFriendClick(friend) })
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Cerrar")
        }
    }
}

// --------- ROW DE UN AMIGO (CON FOTO) ---------

@Composable
private fun FriendRow(
    friend: FriendInfo,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (!friend.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = friend.photoUrl,
                    contentDescription = "Foto de ${friend.username}",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Text(
                    text = (friend.name.takeIf { it.isNotBlank() } ?: friend.username)
                        .first()
                        .uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = friend.name.ifBlank { "Usuario" },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "@${friend.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// --------- MINIPERFIL DE AMIGO EN EL SHEET ---------

@Composable
private fun FriendMiniProfileSheet(
    friendProfile: UserProfile,
    friendsCount: Int,
    onBackToList: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Barra superior
        Box(
            modifier = Modifier
                .size(width = 40.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (!friendProfile.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = friendProfile.photoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Foto de perfil",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Nombre / username / email
        Text(
            text = friendProfile.name.ifBlank { "Usuario" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        if (friendProfile.username.isNotBlank()) {
            Text(
                text = "@${friendProfile.username}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = friendProfile.email,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tarjeta de stats reutilizada
        ProfileStatsCard(
            totalPoints = friendProfile.totalPoints,
            todaySteps = friendProfile.todaySteps,
            stepsPoints = friendProfile.pointsSteps,
            wellnessPoints = friendProfile.pointsWellness,
            tasksPoints = friendProfile.pointsTasks,
            friendsCount = friendsCount
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBackToList) {
                Text("Volver a la lista")
            }
            TextButton(onClick = onClose) {
                Text("Cerrar")
            }
        }
    }
}

// --------- EXTENSIÓN FECHA ---------

private fun Long.toDateString(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}