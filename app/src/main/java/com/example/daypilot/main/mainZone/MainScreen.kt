package com.example.daypilot.main.mainZone

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.main.addFriend.AddFriendScreen
import com.example.daypilot.main.profile.mainProfile.ProfileScreen
import com.example.daypilot.main.profile.settings.SettingsActivity
import androidx.compose.material3.Icon
import com.example.daypilot.R
import com.example.daypilot.main.mainZone.task.TaskActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


enum class MainTab {
    HOME, FRIENDS, PROFILE
}

@Composable
fun MainScreen(
    authRepo: AuthRepository,
    sessionManager: SessionManager,
    onLogoutToLogin: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }
    val user = authRepo.currentUser
    val context = LocalContext.current

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.HOME,
                    onClick = { selectedTab = MainTab.HOME },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.FRIENDS,
                    onClick = { selectedTab = MainTab.FRIENDS },
                    icon = { Icon(Icons.Default.Group, contentDescription = "Amigos") },
                    label = { Text("Amigos") }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.PROFILE,
                    onClick = { selectedTab = MainTab.PROFILE },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeTab()

                MainTab.FRIENDS -> {
                    if (user != null) {
                        AddFriendScreen(
                            authRepo = authRepo,
                            currentUid = user.uid,
                            onBack = { selectedTab = MainTab.HOME }
                        )
                    } else {
                        Text(
                            "Inicia sesión para buscar amigos",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                MainTab.PROFILE -> {
                    if (user != null) {
                        ProfileScreen(
                            authRepo = authRepo,
                            uid = user.uid,
                            accountCreationTimestamp = user.metadata?.creationTimestamp,
                            onOpenSettings = {
                                val intent = Intent(context, SettingsActivity::class.java)
                                context.startActivity(intent)
                            },
                            onBack = { selectedTab = MainTab.HOME }
                        )
                    } else {
                        Text("No hay usuario", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
fun BlockRow(
    leftText: String,
    rightText: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallBlock(
            text = leftText,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
        SmallBlock(
            text = rightText,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
fun BigBlock(
    text: String,
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    subtitle: String? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondary,
    contentColor: Color = MaterialTheme.colorScheme.onSecondary,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    } else {
        modifier.clip(RoundedCornerShape(24.dp))
    }

    Box(
        modifier = clickableModifier.background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (iconResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = iconResId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp)
                    .size(132.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                alpha = 0.18f
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = contentColor.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun SmallBlock(
    text: String,
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    watermarkResId: Int? = null,
    containerColor: Color = MaterialTheme.colorScheme.tertiary,
    contentColor: Color = MaterialTheme.colorScheme.onTertiary,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    } else {
        modifier.clip(RoundedCornerShape(24.dp))
    }

    Box(
        modifier = clickableModifier.background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        if (watermarkResId != null) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = watermarkResId),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(72.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                alpha = 0.20f
            )
        }

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = contentColor
                )
            }

            Text(
                text = text,
                color = contentColor,
                style = MaterialTheme.typography.titleSmall
            )
        }
    }
}

@Composable
fun HomeTab() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1) Calendario (se queda arriba)
            BigBlock(
                text = "Calendario",
                subtitle = "Visualiza tus tareas por días",
                iconResId = R.drawable.calendario,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.40f),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                onClick = {
                    val intent = Intent(
                        context,
                        com.example.daypilot.main.mainZone.calendar.CalendarActivity::class.java
                    )
                    context.startActivity(intent)
                }
            )

            // 2) Hábitos (ahora grande y en medio) + otro color
            BigBlock(
                text = "Hábitos",
                subtitle = "Rutinas y bienestar",
                iconResId = R.drawable.caminar,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.32f),
                containerColor = MaterialTheme.colorScheme.primary,      // 👈 color distinto
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = {
                    val intent = Intent(
                        context,
                        com.example.daypilot.main.mainZone.habits.HabitsActivity::class.java
                    )
                    context.startActivity(intent)
                }
            )

            // 3) Tareas + Rivalidad juntas (Gráficas eliminada)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.28f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallBlock(
                    text = "Tareas",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    watermarkResId = R.drawable.tarea,
                    onClick = {
                        val intent = Intent(context, TaskActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                SmallBlock(
                    text = "Rivalidad",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    watermarkResId = R.drawable.calificacion,
                    onClick = {
                        val intent = Intent(
                            context,
                            com.example.daypilot.main.mainZone.rivalry.RivalryActivity::class.java
                        )
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun PlaceholderTab(label: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label)
    }
}