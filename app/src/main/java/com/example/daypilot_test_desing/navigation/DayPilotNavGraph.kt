package com.example.daypilot_test_desing.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.daypilot_test_desing.ui.model.TaskCategory
import com.example.daypilot_test_desing.ui.model.TaskDifficulty
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.screens.*
import com.example.daypilot_test_desing.ui.model.*
import com.example.daypilot_test_desing.ui.model.TimeZoneRegion

private val tabRoutes = setOf(
    DayPilotDestinations.HOME,
    DayPilotDestinations.FRIENDS,
    DayPilotDestinations.NOTIFICATIONS,
    DayPilotDestinations.PROFILE
)

val MOCK_APPS = listOf(
    AppInfo("YouTube", "com.google.android.youtube"),
    AppInfo("Instagram", "com.instagram.android"),
    AppInfo("TikTok", "com.zhiliaoapp.musically"),
    AppInfo("WhatsApp", "com.whatsapp"),
    AppInfo("Twitter", "com.twitter.android"),
    AppInfo("Facebook", "com.facebook.katana"),
    AppInfo("Spotify", "com.spotify.music"),
    AppInfo("Netflix", "com.netflix.mediaclient"),
    AppInfo("Gmail", "com.google.android.gm"),
    AppInfo("Chrome", "com.android.chrome"),
    AppInfo("Maps", "com.google.android.apps.maps"),
    AppInfo("Telegram", "org.telegram.messenger"),
    AppInfo("Discord", "com.discord"),
    AppInfo("Twitch", "tv.twitch.android.app"),
    AppInfo("Amazon", "com.amazon.mShop.android.shopping")
)

@Composable
fun DayPilotNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute   = backStackEntry.value?.destination?.route
    val showBottomBar  = currentRoute in tabRoutes
    val progressData = List(30) { index ->
        DayProgress(
            day            = index + 1,
            points         = listOf(8, 12, 5, 15, 20, 10, 7, 18, 14, 9,
                11, 16, 6, 22, 13, 8, 19, 11, 7, 15,
                20, 12, 9, 17, 14, 6, 21, 10, 16, 8)[index],
            steps          = listOf(1200, 2500, 800, 3000, 2200, 1500, 900,
                2800, 2100, 1300, 1700, 2400, 700, 3200,
                2000, 1100, 2900, 1600, 850, 2300, 3100,
                1800, 1000, 2600, 2150, 750, 3300, 1400,
                2450, 1050)[index],
            tasksCompleted = listOf(3, 5, 2, 6, 8, 4, 2, 7, 5, 3,
                4, 6, 1, 9, 5, 3, 7, 4, 2, 6,
                8, 5, 3, 7, 5, 1, 8, 4, 6, 3)[index]
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                DayPilotBottomBar(navController = navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = DayPilotDestinations.AUTH,
            modifier         = Modifier.padding(innerPadding)
        ) {

            // ── Auth ─────────────────────────────────────────────
            composable(DayPilotDestinations.AUTH) {
                AuthScreen(
                    onLoginSuccess  = {
                        navController.navigate(DayPilotDestinations.HOME) {
                            popUpTo(DayPilotDestinations.AUTH) { inclusive = true }
                        }
                    },
                    onLoginClick    = { email, password ->
                        navController.navigate(DayPilotDestinations.HOME) {
                            popUpTo(DayPilotDestinations.AUTH) { inclusive = true }
                        }
                    },
                    onRegisterClick = { name, username, email, password, region ->
                    }
                )
            }

            // ── Home ─────────────────────────────────────────────
            composable(DayPilotDestinations.HOME) {
                HomeScreen(
                    userName        = "Mario",
                    streak          = 7,
                    stepsToday      = 1200,
                    stepsGoal       = 2000,
                    tasksCompleted  = 3,
                    tasksTotal      = 5,
                    pointsToday     = 8,
                    rankingPosition = 2,
                    progressData    = progressData.takeLast(7),
                    onNavigateToCalendar = { navController.navigate(DayPilotDestinations.CALENDAR) },
                    onNavigateToHabits   = { navController.navigate(DayPilotDestinations.HABITS) },
                    onNavigateToProgress = { navController.navigate(DayPilotDestinations.PROGRESS) },
                    onNavigateToRivalry  = { navController.navigate(DayPilotDestinations.RIVALRY) }
                )
            }

            // ── Friends ──────────────────────────────────────────
            composable(DayPilotDestinations.FRIENDS) {
                FriendsScreen(
                    friends = listOf(
                        FriendData(
                            id    = "1",
                            name  = "Ana López",
                            email = "ana@example.com",
                            points = 520,
                            streak = 14,
                            weeklySummary = FriendWeeklySummary(
                                totalPoints    = 45,
                                tasksCompleted = 12,
                                totalSteps     = 42000,
                                bestStreak     = 7
                            )
                        ),
                        FriendData(
                            id    = "2",
                            name  = "Carlos Ruiz",
                            email = "carlos@example.com",
                            points = 480,
                            streak = 9,
                            weeklySummary = FriendWeeklySummary(
                                totalPoints    = 38,
                                tasksCompleted = 9,
                                totalSteps     = 35000,
                                bestStreak     = 5
                            )
                        ),
                        FriendData(
                            id    = "3",
                            name  = "Laura Sánchez",
                            email = "laura@example.com",
                            points = 430,
                            streak = 6,
                            weeklySummary = FriendWeeklySummary(
                                totalPoints    = 30,
                                tasksCompleted = 7,
                                totalSteps     = 28000,
                                bestStreak     = 4
                            )
                        )
                    ),
                    friendRequests  = listOf(
                        FriendData("4", "Pedro Martín", "pedro@example.com", 290, 3)
                    ),
                    onAcceptRequest = {},
                    onRejectRequest = {},
                    onTapFriend     = {},
                    onReactToFriend = { _, _ -> },
                    onNavigateToSearch = {
                        navController.navigate(DayPilotDestinations.SEARCH_FRIENDS)
                    }
                )
            }

            composable(DayPilotDestinations.SEARCH_FRIENDS) {
                var results by remember { mutableStateOf<List<SearchUserData>>(emptyList()) }
                var isLoading by remember { mutableStateOf(false) }

                SearchFriendsScreen(
                    searchResults = results,
                    isLoading     = isLoading,
                    onSearch      = { query ->
                        results = listOf(
                            SearchUserData("5", "Mario Pérez",  "mario@test.com",  180, 3),
                            SearchUserData("6", "Sara Gómez",   "sara@test.com",   240, 5),
                            SearchUserData("7", "Juan Martín",  "juan@test.com",   95,  1)
                        ).filter {
                            it.name.contains(query, ignoreCase = true) ||
                                    it.email.contains(query, ignoreCase = true)
                        }
                    },
                    onAddFriend = {},
                    onBack      = { navController.popBackStack() }
                )
            }

            // ── Notifications ─────────────────────────────────────
            composable(DayPilotDestinations.NOTIFICATIONS) {
                NotificationsScreen(
                    notifications = listOf(
                        NotificationData("1", "¡Tarea completada!",  "Has completado 'Salir a correr'",          "hace 5min", NotificationType.TASK,        false),
                        NotificationData("2", "Nueva solicitud",      "Ana López quiere ser tu amiga",            "hace 1h",   NotificationType.SOCIAL,      false),
                        NotificationData("3", "¡Meta de pasos!",      "Has alcanzado el 75% de tu meta",          "hace 2h",   NotificationType.STEPS,       true),
                        NotificationData("4", "Racha de 7 días 🔥",   "Llevas 7 días cumpliendo objetivos",       "ayer",      NotificationType.STREAK,      true),
                        NotificationData("5", "Recordatorio",         "Tienes 3 tareas pendientes para hoy",      "ayer",      NotificationType.REMINDER,    true),
                        NotificationData("6", "¡Nuevo logro!",        "Has alcanzado el nivel 7",                 "hace 2d",   NotificationType.ACHIEVEMENT, true)
                    ),
                    onTapNotification = {}
                )
            }

            // ── Profile ───────────────────────────────────────────
            composable(DayPilotDestinations.PROFILE) {
                ProfileScreen(
                    name             = "Mario García",
                    username         = "mariogarcia",
                    email            = "mario@example.com",
                    memberSince      = "06/04/2026",
                    level            = 7,
                    totalPoints      = 340,
                    currentStreak    = 7,
                    longestStreak    = 14,
                    rankingPosition  = 2,
                    pointsToday      = 8,
                    pointsFromTasks  = 4,
                    pointsFromSteps  = 2,
                    pointsFromHabits = 1,
                    pointsFromTimers = 1,
                    weeklySummary = WeeklySummaryData(
                        totalPoints    = 45,
                        tasksCompleted = 12,
                        totalSteps     = 42000,
                        bestStreak     = 7,
                        reactions      = listOf(
                            ReceivedReaction("Ana López",   ReactionType.CLAP),
                            ReceivedReaction("Carlos Ruiz", ReactionType.FIRE)
                        )
                    ),
                    onNavigateToSettings    = { navController.navigate(DayPilotDestinations.SETTINGS) }
                )
            }

            // ── Settings ──────────────────────────────────────────
            composable(DayPilotDestinations.SETTINGS) {
                var isDarkMode           by remember { mutableStateOf(true) }
                var selectedTheme        by remember { mutableStateOf("sage_green") }
                var selectedLanguage     by remember { mutableStateOf("Español") }
                var notificationsEnabled by remember { mutableStateOf(true) }

                SettingsScreen(
                    name                  = "Mario García",
                    isDarkMode            = isDarkMode,
                    selectedThemeId       = selectedTheme,
                    selectedLanguage      = selectedLanguage,
                    notificationsEnabled  = notificationsEnabled,
                    onToggleDarkMode      = { isDarkMode = it },
                    onThemeSelect         = { selectedTheme = it },
                    onLanguageSelect      = { selectedLanguage = it },
                    onToggleNotifications = { notificationsEnabled = it },
                    onNavigateToEditProfile = { navController.navigate(DayPilotDestinations.EDIT_PROFILE) },
                    onLogout              = {
                        navController.navigate(DayPilotDestinations.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(DayPilotDestinations.EDIT_PROFILE) {
                EditProfileScreen(
                    currentName     = "Mario García",
                    currentUsername = "mariogarcia",
                    currentRegion = TimeZoneRegion.EUROPE_MADRID,
                    onSave          = { _, _, _ -> navController.popBackStack() },
                    onNavigateToResetPassword = {
                        navController.navigate(DayPilotDestinations.RESET_PASSWORD)
                    },
                    onPickFromCamera  = {},
                    onPickFromGallery = {},
                    onBack            = { navController.popBackStack() }
                )
            }

            composable(DayPilotDestinations.RESET_PASSWORD) {
                ResetPasswordScreen(
                    onSendResetEmail = { _ -> },
                    onBack           = { navController.popBackStack() }
                )
            }

            // ── Calendar ──────────────────────────────────────────
            composable(DayPilotDestinations.CALENDAR) {
                var tasks by remember {
                    mutableStateOf(
                        listOf(
                            CalendarTaskData("1", 19, "Terminar TFG",      TaskCategory.STUDY, TaskDifficulty.HARD,   120, false),
                            CalendarTaskData("2", 19, "Salir a correr",    TaskCategory.SPORT, TaskDifficulty.EASY,   45,  true),
                            CalendarTaskData("3", 19, "Preparar cena",     TaskCategory.HOME,  TaskDifficulty.EASY,   30,  false),
                            CalendarTaskData("4", 15, "Reunión de equipo", TaskCategory.WORK,  TaskDifficulty.MEDIUM, 60,  false),
                            CalendarTaskData("5", 20, "Comprar comida",    TaskCategory.HOME,  TaskDifficulty.EASY,   30,  false),
                            CalendarTaskData("6", 22, "Presentación",      TaskCategory.STUDY, TaskDifficulty.HARD,   180, false)
                        )
                    )
                }

                CalendarScreen(
                    tasks        = tasks,
                    onBack       = { navController.popBackStack() },
                    onAddTask    = {},
                    onTapTask    = {},
                    onToggleTask = { id, done ->
                        tasks = tasks.map { if (it.id == id) it.copy(isDone = done) else it }
                    },
                    onDeleteTask = { id ->
                        tasks = tasks.filter { it.id != id }
                    },
                    onEditTask   = {}
                )
            }

            // ── Habits ───────────────────────────────────────────
            composable(DayPilotDestinations.HABITS) {
                HabitsScreen(
                    currentSteps      = 1200,
                    goalSteps         = 2000,
                    pointsEarned      = 1,
                    pointsRemaining   = 5,
                    onBack            = { navController.popBackStack() },
                    onNavigateToSteps = { navController.navigate(DayPilotDestinations.STEPS) },
                    onNavigateToTimer = { navController.navigate(DayPilotDestinations.TIMER_HUB) },
                    onNavigateToReminders  = { navController.navigate(DayPilotDestinations.REMINDERS) },
                    onNavigateToTechHealth = { navController.navigate(DayPilotDestinations.TECH_HEALTH) }
                )
            }

            composable(DayPilotDestinations.TIMER_HUB) {
                TimerHubScreen(
                    onNavigateToTimer    = { id, minutes ->
                        navController.navigate(DayPilotDestinations.timerRoute(id, minutes))
                    },
                    onNavigateToPomodoro = { sessions ->
                        navController.navigate(DayPilotDestinations.pomodoroRoute(sessions))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Progress ─────────────────────────────────────────
            composable(DayPilotDestinations.PROGRESS) {
                ProgressScreen(
                    progressData     = progressData,
                    rankingPosition  = 2,
                    pointsToday      = 8,
                    pointsFromTasks  = 4,
                    pointsFromSteps  = 2,
                    pointsFromHabits = 1,
                    pointsFromTimers = 1,
                    onBack           = { navController.popBackStack() }
                )
            }

            // ── Rivalry ──────────────────────────────────────────
            composable(DayPilotDestinations.RIVALRY) {
                RivalryScreen(
                    currentUserName     = "Mario García",
                    currentUserPosition = 2,
                    currentUserPoints   = 340,
                    currentUserStreak   = 7,
                    ranking = listOf(
                        RankingData("1", "Ana López", 520, 14),
                        RankingData("2", "Mario García",  340, 7),
                        RankingData("3", "Carlos Ruiz",   310, 9),
                        RankingData("4", "Laura Sánchez", 280, 4),
                        RankingData("5", "Pedro Martín",  190, 2)
                    ),
                    onBack = { navController.popBackStack() }
                )
            }

            // ── Steps ────────────────────────────────────────────
            composable(DayPilotDestinations.STEPS) {
                StepsScreen(
                    currentSteps    = 1200,
                    goalSteps       = 2000,
                    pointsEarned    = 1,
                    pointsRemaining = 5,
                    totalSteps7Days = 42000,
                    bestDaySteps    = 8500,
                    dailyAverage    = 6000,
                    goalStreak      = 4,
                    onBack          = { navController.popBackStack() },
                    onConfigureGoal = { _ -> }
                )
            }

            // ── Timer ────────────────────────────────────────────
            composable(
                route     = DayPilotDestinations.TIMER,
                arguments = listOf(
                    navArgument("timerMode") { type = NavType.StringType },
                    navArgument("minutes")   { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val mode    = backStackEntry.arguments?.getString("timerMode") ?: "TRAINING"
                val minutes = backStackEntry.arguments?.getInt("minutes") ?: 30
                TimerScreen(
                    timerMode        = mode,
                    customMinutes    = minutes,
                    pointEarnedToday = false,
                    onBack           = { navController.popBackStack() }
                )
            }

            composable(
                route     = DayPilotDestinations.POMODORO,
                arguments = listOf(navArgument("sessions") { type = NavType.IntType })
            ) { backStackEntry ->
                val sessions = backStackEntry.arguments?.getInt("sessions") ?: 4
                PomodoroScreen(
                    totalSessions = sessions,
                    onBack        = { navController.popBackStack() }
                )
            }

            // ── Reminders ────────────────────────────────────────
            composable(DayPilotDestinations.REMINDERS) {
                var reminders by remember {
                    mutableStateOf(
                        listOf(
                            ReminderData("1", "Ejercicio matutino", "07:30", "30 minutos de cardio", true),
                            ReminderData("2", "Tomar agua",         "12:00", "2 litros al día",      true),
                            ReminderData("3", "Meditación",         "22:00", "15 minutos",           false)
                        )
                    )
                }
                RemindersScreen(
                    reminders        = reminders,
                    onAddReminder    = { _ -> },
                    onDeleteReminder = { id -> reminders = reminders.filter { it.id != id } },
                    onToggleReminder = { id, enabled ->
                        reminders = reminders.map {
                            if (it.id == id) it.copy(isEnabled = enabled) else it
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            // ── TechHealth ───────────────────────────────────────
            composable(DayPilotDestinations.TECH_HEALTH) {
                var appRestrictions   by remember {
                    mutableStateOf(
                        listOf(
                            AppRestriction(
                                id                          = "1",
                                appName                     = "YouTube",
                                packageName                 = "com.google.android.youtube",
                                dailyLimitMinutes           = 120,
                                notificationIntervalSeconds = 60,
                                isEnabled                   = true,
                                usedMinutesToday            = 45
                            )
                        )
                    )
                }
                var groupRestrictions by remember { mutableStateOf<List<GroupRestriction>>(emptyList()) }

                TechHealthScreen(
                    appRestrictions     = appRestrictions,
                    groupRestrictions   = groupRestrictions,
                    onSaveApp           = { restriction, isEdit ->
                        appRestrictions = if (isEdit) {
                            appRestrictions.map {
                                if (it.id == restriction.id) restriction else it
                            }
                        } else {
                            appRestrictions + restriction
                        }
                    },
                    onSaveGroup         = { group, isEdit ->
                        groupRestrictions = if (isEdit) {
                            groupRestrictions.map {
                                if (it.id == group.id) group else it
                            }
                        } else {
                            groupRestrictions + group
                        }
                    },
                    onToggleRestriction = { id, enabled ->
                        appRestrictions = appRestrictions.map {
                            if (it.id == id) it.copy(isEnabled = enabled) else it
                        }
                    },
                    onDeleteRestriction = { id ->
                        appRestrictions = appRestrictions.map {
                            if (it.id == id) it.copy(pendingDelete = true) else it
                        }
                    },
                    onToggleGroup       = { id, enabled ->
                        groupRestrictions = groupRestrictions.map {
                            if (it.id == id) it.copy(isEnabled = enabled) else it
                        }
                    },
                    onDeleteGroup       = { id ->
                        groupRestrictions = groupRestrictions.map {
                            if (it.id == id) it.copy(pendingDelete = true) else it
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}