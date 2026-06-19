package com.example.daypilot_test_desing.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.daypilot_test_desing.ui.components.basic.AppInfo
import com.example.daypilot_test_desing.presentation.calendar.CalendarViewModel
import com.example.daypilot_test_desing.presentation.friends.FriendsViewModel
import com.example.daypilot_test_desing.presentation.friends.SearchFriendsViewModel
import com.example.daypilot_test_desing.presentation.habits.HabitsViewModel
import com.example.daypilot_test_desing.presentation.habits.StepsViewModel
import com.example.daypilot_test_desing.presentation.home.HomeViewModel
import com.example.daypilot_test_desing.presentation.notifications.NotificationsViewModel
import com.example.daypilot_test_desing.presentation.profile.ProfileViewModel
import com.example.daypilot_test_desing.presentation.progress.ProgressViewModel
import com.example.daypilot_test_desing.presentation.reminders.RemindersViewModel
import com.example.daypilot_test_desing.presentation.rivalry.RivalryViewModel
import com.example.daypilot_test_desing.presentation.settings.SettingsViewModel
import com.example.daypilot_test_desing.presentation.techhealth.TechHealthViewModel
import com.example.daypilot_test_desing.presentation.auth.AuthScreen
import com.example.daypilot_test_desing.presentation.calendar.CalendarScreen
import com.example.daypilot_test_desing.presentation.profile.EditProfileScreen
import com.example.daypilot_test_desing.presentation.friends.FriendsScreen
import com.example.daypilot_test_desing.presentation.habits.HabitsScreen
import com.example.daypilot_test_desing.presentation.home.HomeScreen
import com.example.daypilot_test_desing.presentation.notifications.NotificationsScreen
import com.example.daypilot_test_desing.presentation.timer.PomodoroScreen
import com.example.daypilot_test_desing.presentation.profile.ProfileScreen
import com.example.daypilot_test_desing.presentation.progress.ProgressScreen
import com.example.daypilot_test_desing.presentation.reminders.RemindersScreen
import com.example.daypilot_test_desing.presentation.auth.ResetPasswordScreen
import com.example.daypilot_test_desing.presentation.rivalry.RivalryScreen
import com.example.daypilot_test_desing.presentation.friends.SearchFriendsScreen
import com.example.daypilot_test_desing.presentation.settings.SettingsScreen
import com.example.daypilot_test_desing.presentation.habits.StepsScreen
import com.example.daypilot_test_desing.presentation.techhealth.TechHealthScreen
import com.example.daypilot_test_desing.presentation.timer.TimerHubScreen
import com.example.daypilot_test_desing.presentation.timer.TimerScreen

private val tabRoutes = setOf(
    DayPilotDestinations.HOME,
    DayPilotDestinations.FRIENDS,
    DayPilotDestinations.NOTIFICATIONS,
    DayPilotDestinations.PROFILE
)

val MOCK_APPS = listOf(
    AppInfo("YouTube",   "com.google.android.youtube"),
    AppInfo("Instagram", "com.instagram.android"),
    AppInfo("TikTok",    "com.zhiliaoapp.musically"),
    AppInfo("WhatsApp",  "com.whatsapp"),
    AppInfo("Twitter",   "com.twitter.android"),
    AppInfo("Facebook",  "com.facebook.katana"),
    AppInfo("Spotify",   "com.spotify.music"),
    AppInfo("Netflix",   "com.netflix.mediaclient"),
    AppInfo("Gmail",     "com.google.android.gm"),
    AppInfo("Chrome",    "com.android.chrome"),
    AppInfo("Maps",      "com.google.android.apps.maps"),
    AppInfo("Telegram",  "org.telegram.messenger"),
    AppInfo("Discord",   "com.discord"),
    AppInfo("Twitch",    "tv.twitch.android.app"),
    AppInfo("Amazon",    "com.amazon.mShop.android.shopping")
)

@Composable
fun DayPilotNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // ViewModels scoped to the NavGraph lifetime
    val homeVM: HomeViewModel                   = viewModel()
    val calendarVM: CalendarViewModel           = viewModel()
    val friendsVM: FriendsViewModel             = viewModel()
    val searchVM: SearchFriendsViewModel        = viewModel()
    val notificationsVM: NotificationsViewModel = viewModel()
    val profileVM: ProfileViewModel             = viewModel()
    val settingsVM: SettingsViewModel           = viewModel()
    val habitsVM: HabitsViewModel               = viewModel()
    val stepsVM: StepsViewModel                 = viewModel()
    val progressVM: ProgressViewModel           = viewModel()
    val rivalryVM: RivalryViewModel             = viewModel()
    val remindersVM: RemindersViewModel         = viewModel()
    val techHealthVM: TechHealthViewModel       = viewModel()

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = currentRoute in tabRoutes,
                enter   = slideInVertically(initialOffsetY = { it }),
                exit    = slideOutVertically(targetOffsetY = { it })
            ) {
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
                    onLoginClick    = { _, _ ->
                        navController.navigate(DayPilotDestinations.HOME) {
                            popUpTo(DayPilotDestinations.AUTH) { inclusive = true }
                        }
                    },
                    onRegisterClick = { _, _, _, _, _ -> }
                )
            }

            // ── Home ─────────────────────────────────────────────
            composable(DayPilotDestinations.HOME) {
                val s by homeVM.uiState.collectAsState()
                HomeScreen(
                    userName            = s.userName,
                    streak              = s.streak,
                    stepsToday          = s.stepsToday,
                    stepsGoal           = s.stepsGoal,
                    tasksCompleted      = s.tasksCompleted,
                    tasksTotal          = s.tasksTotal,
                    progressData        = s.progressData,
                    pointsToday         = s.pointsToday,
                    rankingPosition     = s.rankingPosition,
                    onNavigateToCalendar= { navController.navigate(DayPilotDestinations.CALENDAR) },
                    onNavigateToHabits  = { navController.navigate(DayPilotDestinations.HABITS) },
                    onNavigateToProgress= { navController.navigate(DayPilotDestinations.PROGRESS) },
                    onNavigateToRivalry = { navController.navigate(DayPilotDestinations.RIVALRY) }
                )
            }

            // ── Friends ──────────────────────────────────────────
            composable(DayPilotDestinations.FRIENDS) {
                val s by friendsVM.uiState.collectAsState()
                FriendsScreen(
                    friends            = s.friends,
                    friendRequests     = s.friendRequests,
                    onAcceptRequest    = friendsVM::acceptRequest,
                    onRejectRequest    = friendsVM::rejectRequest,
                    onTapFriend        = {},
                    onReactToFriend    = friendsVM::reactToFriend,
                    onNavigateToSearch = { navController.navigate(DayPilotDestinations.SEARCH_FRIENDS) }
                )
            }

            composable(DayPilotDestinations.SEARCH_FRIENDS) {
                val s by searchVM.uiState.collectAsState()
                SearchFriendsScreen(
                    searchResults = s.searchResults,
                    isLoading     = s.isLoading,
                    onSearch      = searchVM::search,
                    onAddFriend   = { userId ->
                        searchVM.addFriend(userId)
                        friendsVM.refresh()
                    },
                    onBack        = { navController.popBackStack() }
                )
            }

            // ── Notifications ─────────────────────────────────────
            composable(DayPilotDestinations.NOTIFICATIONS) {
                val s by notificationsVM.uiState.collectAsState()
                NotificationsScreen(
                    notifications    = s.notifications,
                    onTapNotification= notificationsVM::markAsRead
                )
            }

            // ── Profile ───────────────────────────────────────────
            composable(DayPilotDestinations.PROFILE) {
                val s by profileVM.uiState.collectAsState()
                ProfileScreen(
                    name             = s.name,
                    username         = s.username,
                    email            = s.email,
                    memberSince      = s.memberSince,
                    level            = s.level,
                    totalPoints      = s.totalPoints,
                    currentStreak    = s.currentStreak,
                    longestStreak    = s.longestStreak,
                    rankingPosition  = s.rankingPosition,
                    pointsToday      = s.pointsToday,
                    pointsFromTasks  = s.pointsFromTasks,
                    pointsFromSteps  = s.pointsFromSteps,
                    pointsFromHabits = s.pointsFromHabits,
                    pointsFromTimers = s.pointsFromTimers,
                    avatarUrl        = s.avatarUrl,
                    weeklySummary    = s.weeklySummary,
                    onNavigateToSettings = { navController.navigate(DayPilotDestinations.SETTINGS) }
                )
            }

            // ── Settings ──────────────────────────────────────────
            composable(DayPilotDestinations.SETTINGS) {
                val s by settingsVM.uiState.collectAsState()
                SettingsScreen(
                    name                 = s.name,
                    isDarkMode           = s.isDarkMode,
                    selectedThemeId      = s.selectedThemeId,
                    selectedLanguage     = s.selectedLanguage,
                    notificationsEnabled = s.notificationsEnabled,
                    onToggleDarkMode     = settingsVM::toggleDarkMode,
                    onThemeSelect        = settingsVM::selectTheme,
                    onLanguageSelect     = settingsVM::selectLanguage,
                    onToggleNotifications= settingsVM::toggleNotifications,
                    onNavigateToEditProfile = { navController.navigate(DayPilotDestinations.EDIT_PROFILE) },
                    onLogout = {
                        navController.navigate(DayPilotDestinations.AUTH) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(DayPilotDestinations.EDIT_PROFILE) {
                val s by profileVM.uiState.collectAsState()
                EditProfileScreen(
                    currentName     = s.name,
                    currentUsername = s.username,
                    avatarUrl       = s.avatarUrl,
                    onSave          = { name, username, region ->
                        profileVM.updateProfile(name, username, region)
                        navController.popBackStack()
                    },
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
                val s by calendarVM.uiState.collectAsState()
                CalendarScreen(
                    tasks         = s.tasks,
                    onBack        = { homeVM.refresh(); navController.popBackStack() },
                    onCreateTask  = calendarVM::addTask,
                    onTapTask     = {},
                    onToggleTask  = calendarVM::toggleTask,
                    onDeleteTask  = calendarVM::deleteTask,
                    onEditTask    = calendarVM::editTask
                )
            }

            // ── Habits ───────────────────────────────────────────
            composable(DayPilotDestinations.HABITS) {
                val s by habitsVM.uiState.collectAsState()
                HabitsScreen(
                    currentSteps          = s.currentSteps,
                    goalSteps             = s.goalSteps,
                    pointsEarned          = s.pointsEarned,
                    pointsRemaining       = s.pointsRemaining,
                    onBack                = { navController.popBackStack() },
                    onNavigateToSteps     = { navController.navigate(DayPilotDestinations.STEPS) },
                    onNavigateToTimer     = { navController.navigate(DayPilotDestinations.TIMER_HUB) },
                    onNavigateToReminders = { navController.navigate(DayPilotDestinations.REMINDERS) },
                    onNavigateToTechHealth= { navController.navigate(DayPilotDestinations.TECH_HEALTH) }
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
                val s by progressVM.uiState.collectAsState()
                ProgressScreen(
                    progressData    = s.progressData,
                    rankingPosition = s.rankingPosition,
                    pointsToday     = s.pointsToday,
                    pointsFromTasks = s.pointsFromTasks,
                    pointsFromSteps = s.pointsFromSteps,
                    pointsFromHabits= s.pointsFromHabits,
                    pointsFromTimers= s.pointsFromTimers,
                    onBack          = { navController.popBackStack() }
                )
            }

            // ── Rivalry ──────────────────────────────────────────
            composable(DayPilotDestinations.RIVALRY) {
                val s by rivalryVM.uiState.collectAsState()
                RivalryScreen(
                    currentUserName    = s.currentUserName,
                    currentUserPosition= s.currentUserPosition,
                    currentUserPoints  = s.currentUserPoints,
                    currentUserStreak  = s.currentUserStreak,
                    ranking            = s.ranking,
                    onBack             = { navController.popBackStack() }
                )
            }

            // ── Steps ────────────────────────────────────────────
            composable(DayPilotDestinations.STEPS) {
                val s by stepsVM.uiState.collectAsState()
                StepsScreen(
                    currentSteps   = s.currentSteps,
                    goalSteps      = s.goalSteps,
                    pointsEarned   = s.pointsEarned,
                    pointsRemaining= s.pointsRemaining,
                    totalSteps7Days= s.totalSteps7Days,
                    bestDaySteps   = s.bestDaySteps,
                    dailyAverage   = s.dailyAverage,
                    goalStreak     = s.goalStreak,
                    onBack         = { habitsVM.refresh(); navController.popBackStack() },
                    onConfigureGoal= stepsVM::configureGoal
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
                val s by remindersVM.uiState.collectAsState()
                RemindersScreen(
                    reminders       = s.reminders,
                    onAddReminder   = remindersVM::addReminder,
                    onDeleteReminder= remindersVM::deleteReminder,
                    onToggleReminder= remindersVM::toggleReminder,
                    onBack          = { navController.popBackStack() }
                )
            }

            // ── TechHealth ───────────────────────────────────────
            composable(DayPilotDestinations.TECH_HEALTH) {
                val s by techHealthVM.uiState.collectAsState()
                TechHealthScreen(
                    appRestrictions   = s.appRestrictions,
                    groupRestrictions = s.groupRestrictions,
                    onSaveApp         = { restriction, _ -> techHealthVM.saveApp(restriction) },
                    onSaveGroup       = { group, _        -> techHealthVM.saveGroup(group) },
                    onToggleRestriction = techHealthVM::toggleRestriction,
                    onDeleteRestriction = techHealthVM::deleteRestriction,
                    onToggleGroup       = techHealthVM::toggleGroup,
                    onDeleteGroup       = techHealthVM::deleteGroup,
                    onBack            = { navController.popBackStack() }
                )
            }
        }
    }
}
