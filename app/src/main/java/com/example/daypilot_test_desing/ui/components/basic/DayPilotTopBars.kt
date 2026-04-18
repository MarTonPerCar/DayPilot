package com.example.daypilot_test_desing.ui.components.basic

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight

// ── 1. TopBar simple con título ──────────────────────────────────
@Composable
fun DayPilotTopBar(
    title: String,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint               = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── 2. TopBar con una acción ─────────────────────────────────────
@Composable
fun DayPilotTopBarWithAction(
    title: String,
    actionIcon: ImageVector,
    actionDescription: String = "",
    onAction: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint               = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onAction) {
                Icon(
                    imageVector        = actionIcon,
                    contentDescription = actionDescription,
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── 3. TopBar con dos acciones ───────────────────────────────────
@Composable
fun DayPilotTopBarWithTwoActions(
    title: String,
    firstActionIcon: ImageVector,
    firstActionDescription: String = "",
    onFirstAction: () -> Unit,
    secondActionIcon: ImageVector,
    secondActionDescription: String = "",
    onSecondAction: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    TopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint               = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = onFirstAction) {
                Icon(
                    imageVector        = firstActionIcon,
                    contentDescription = firstActionDescription,
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
            IconButton(onClick = onSecondAction) {
                Icon(
                    imageVector        = secondActionIcon,
                    contentDescription = secondActionDescription,
                    tint               = MaterialTheme.colorScheme.onBackground
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

// ── 4. TopBar centrada ───────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPilotCenteredTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionDescription: String = "",
    onAction: (() -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text       = title,
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onBackground
            )
        },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector        = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint               = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        actions = {
            if (actionIcon != null && onAction != null) {
                IconButton(onClick = onAction) {
                    Icon(
                        imageVector        = actionIcon,
                        contentDescription = actionDescription,
                        tint               = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}