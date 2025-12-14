// com.example.daypilot.main.MainScreen.kt
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.daypilot.mainDatabase.SessionManager
import com.example.daypilot.firebaseLogic.authLogic.AuthRepository
import com.example.daypilot.main.addFriend.AddFriendScreen
import com.example.daypilot.main.profile.ProfileScreen
import com.example.daypilot.main.profile.SettingsActivity
import androidx.compose.material3.Icon
import com.example.daypilot.R
import com.example.daypilot.main.mainZone.task.TaskActivity


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
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.secondary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSecondary,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SmallBlock(
    text: String,
    modifier: Modifier = Modifier,
    iconResId: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val clickableModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() }
    } else {
        modifier
            .clip(RoundedCornerShape(24.dp))
    }

    Box(
        modifier = clickableModifier
            .background(MaterialTheme.colorScheme.tertiary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(12.dp)
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onTertiary
                )
            }

            Text(
                text = text,
                color = MaterialTheme.colorScheme.onTertiary,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center
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
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Spacer(Modifier.height(12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Grande ≈ 45% del alto útil
            BigBlock(
                text = "Grande",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.45f)
            )

            // Fila media: Tareas + D
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.275f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SmallBlock(
                    text = "Tareas",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    iconResId = R.drawable.tarea,   // 👈 tu icono tarea.png en drawable
                    onClick = {
                        val intent = Intent(context, TaskActivity::class.java)
                        context.startActivity(intent)
                    }
                )

                SmallBlock(
                    text = "D",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                    // aquí luego meteremos la gráfica de puntos
                )
            }

            // Fila inferior ≈ 27.5%
            BlockRow(
                leftText = "Otro 1",
                rightText = "Otro 2",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.275f)
            )
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