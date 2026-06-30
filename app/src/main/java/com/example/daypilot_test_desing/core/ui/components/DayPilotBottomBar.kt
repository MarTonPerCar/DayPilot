package com.example.daypilot_test_desing.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.daypilot_test_desing.navigation.DayPilotDestinations

// ── Tabs de la barra ─────────────────────────────────────────────
data class BottomBarTab(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val iconSelected: ImageVector = icon
)

val bottomBarTabs = listOf(
    BottomBarTab(
        route        = DayPilotDestinations.HOME,
        label        = "Inicio",
        icon         = Icons.Default.Home,
    ),
    BottomBarTab(
        route        = DayPilotDestinations.FRIENDS,
        label        = "Amigos",
        icon         = Icons.Default.People,
    ),
    BottomBarTab(
        route        = DayPilotDestinations.NOTIFICATIONS,
        label        = "Avisos",
        icon         = Icons.Default.Notifications,
    ),
    BottomBarTab(
        route        = DayPilotDestinations.PROFILE,
        label        = "Perfil",
        icon         = Icons.Default.Person,
    )
)

// Maps any route to the tab that "owns" it
private fun rootTabFor(route: String?): String? = when (route) {
    DayPilotDestinations.HOME,
    DayPilotDestinations.CALENDAR,
    DayPilotDestinations.HABITS,
    DayPilotDestinations.STEPS,
    DayPilotDestinations.PROGRESS,
    DayPilotDestinations.RIVALRY,
    DayPilotDestinations.TIMER_HUB,
    DayPilotDestinations.TIMER,
    DayPilotDestinations.POMODORO,
    DayPilotDestinations.REMINDERS,
    DayPilotDestinations.TECH_HEALTH -> DayPilotDestinations.HOME
    DayPilotDestinations.FRIENDS,
    DayPilotDestinations.SEARCH_FRIENDS -> DayPilotDestinations.FRIENDS
    DayPilotDestinations.NOTIFICATIONS -> DayPilotDestinations.NOTIFICATIONS
    DayPilotDestinations.PROFILE,
    DayPilotDestinations.SETTINGS,
    DayPilotDestinations.EDIT_PROFILE,
    DayPilotDestinations.RESET_PASSWORD -> DayPilotDestinations.PROFILE
    else -> null
}

// ── Bottom Bar ───────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayPilotBottomBar(navController: NavController, unreadNotifications: Int = 0) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry?.destination?.route
    val activeTab      = rootTabFor(currentRoute)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        bottomBarTabs.forEach { tab ->
            val isSelected = activeTab == tab.route

            val iconScale by animateFloatAsState(
                targetValue   = if (isSelected) 1.15f else 1f,
                animationSpec = tween(200),
                label         = "icon_scale_${tab.route}"
            )
            val labelColor by animateColorAsState(
                targetValue   = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                animationSpec = tween(200),
                label         = "label_color_${tab.route}"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick  = {
                    if (activeTab == tab.route) {
                        // Already in this tab — pop sub-screens to return to tab root
                        if (currentRoute != tab.route) {
                            navController.popBackStack(tab.route, inclusive = false)
                        }
                    } else {
                        navController.navigate(tab.route) {
                            popUpTo(DayPilotDestinations.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState    = true
                        }
                    }
                },
                icon = {
                    val showBadge = tab.route == DayPilotDestinations.NOTIFICATIONS &&
                        unreadNotifications > 0
                    BadgedBox(
                        badge = {
                            if (showBadge) {
                                Badge {
                                    Text(if (unreadNotifications > 9) "9+" else "$unreadNotifications")
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector        = tab.icon,
                            contentDescription = tab.label,
                            modifier           = Modifier.scale(iconScale)
                        )
                    }
                },
                label = {
                    Text(
                        text       = tab.label,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = labelColor
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor      = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            )
        }
    }
}