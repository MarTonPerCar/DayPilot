package com.example.daypilot_test_desing.navigation

import android.app.LocaleManager
import android.os.Build
import android.os.LocaleList
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import android.app.Application
import android.content.Context
import androidx.compose.runtime.remember
import kotlinx.coroutines.joinAll
import com.example.daypilot_test_desing.backend.local.NotificationHub
import com.example.daypilot_test_desing.backend.model.NotificationType
import com.example.daypilot_test_desing.backend.preferences.AppPreferences
import com.example.daypilot_test_desing.reminders.createDailyChannel
import com.example.daypilot_test_desing.reminders.DailyNotificationScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.daypilot_test_desing.backend.supabase.SupabaseFriendRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseNotificationRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseProgressRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseRankingRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseStepsRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseTaskRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseUserRepository
import com.example.daypilot_test_desing.viewmodel.AppSessionViewModel
import com.example.daypilot_test_desing.viewmodel.auth.AuthViewModel
import com.example.daypilot_test_desing.viewmodel.calendar.CalendarViewModel
import com.example.daypilot_test_desing.viewmodel.friends.FriendsViewModel
import com.example.daypilot_test_desing.viewmodel.friends.SearchFriendsViewModel
import com.example.daypilot_test_desing.viewmodel.habits.HabitsViewModel
import com.example.daypilot_test_desing.viewmodel.habits.StepsViewModel
import com.example.daypilot_test_desing.viewmodel.home.HomeViewModel
import com.example.daypilot_test_desing.viewmodel.notifications.NotificationsViewModel
import com.example.daypilot_test_desing.viewmodel.profile.ProfileViewModel
import com.example.daypilot_test_desing.viewmodel.progress.ProgressViewModel
import com.example.daypilot_test_desing.viewmodel.reminders.RemindersViewModel
import com.example.daypilot_test_desing.viewmodel.rivalry.RivalryViewModel
import com.example.daypilot_test_desing.viewmodel.settings.SettingsViewModel
import com.example.daypilot_test_desing.viewmodel.techhealth.TechHealthViewModel
import com.example.daypilot_test_desing.presentation.auth.AuthScreen
import com.example.daypilot_test_desing.presentation.loading.LoadingScreen
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


@Composable
fun DayPilotNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    // ViewModels scoped to the NavGraph lifetime
    val sessionVM: AppSessionViewModel          = viewModel()
    val authVM: AuthViewModel                   = viewModel()
    val notificationsVM: NotificationsViewModel = viewModel()
    val remindersVM: RemindersViewModel         = viewModel()
    val techHealthVM: TechHealthViewModel       = viewModel()

    val context = LocalContext.current
    val application = context.applicationContext as Application
    val appPrefs = remember { AppPreferences(context) }

    // Shared repository instances — created once, passed to all ViewModels that need them.
    val stepsRepo    = remember { SupabaseStepsRepository(application.getSharedPreferences("daypilot_steps", Context.MODE_PRIVATE)) }
    val progressRepo = remember { SupabaseProgressRepository() }
    val userRepo     = remember { SupabaseUserRepository() }
    val friendRepo   = remember { SupabaseFriendRepository() }
    val rankingRepo  = remember { SupabaseRankingRepository() }

    val homeVM: HomeViewModel            = viewModel(factory = HomeViewModel.factory(stepsRepo, progressRepo, userRepo, friendRepo, SupabaseTaskRepository()))
    val friendsVM: FriendsViewModel      = viewModel(factory = FriendsViewModel.factory(friendRepo))
    val searchVM: SearchFriendsViewModel = viewModel(factory = SearchFriendsViewModel.factory(friendRepo))
    val rivalryVM: RivalryViewModel      = viewModel(factory = RivalryViewModel.factory(rankingRepo))
    val habitsVM: HabitsViewModel = viewModel(factory = HabitsViewModel.factory(stepsRepo))
    val stepsVM: StepsViewModel   = viewModel(factory = StepsViewModel.factory(application, stepsRepo))
    val progressVM: ProgressViewModel = viewModel(factory = ProgressViewModel.factory(application, progressRepo))
    val profileVM: ProfileViewModel   = viewModel(factory = ProfileViewModel.factory(userRepo, progressRepo))
    val settingsVM: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(application, userRepo))
    val calendarVM: CalendarViewModel = viewModel(
        factory = CalendarViewModel.factory(SupabaseTaskRepository(), progressRepo)
    )

    // Propagate sensor step updates to HabitsScreen and HomeScreen
    val stepsState by stepsVM.uiState.collectAsState()
    LaunchedEffect(stepsState.currentSteps) {
        habitsVM.refresh()
        homeVM.refresh()
    }

    // Refresh home data every time the user lands on HOME so friend/ranking
    // counts are always up-to-date (avoids race conditions with accept/remove
    // friend callbacks that fire before the Supabase write completes).
    LaunchedEffect(currentRoute) {
        if (currentRoute == DayPilotDestinations.HOME) homeVM.refresh()
    }

    // Track that the app was opened today (for streak-danger alarm check).
    LaunchedEffect(Unit) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
        appPrefs.lastOpenDate = today
        createDailyChannel(context)
    }

    // Level-up in-app notification: fire when profileVM.level increases.
    val profileStateForLevel by profileVM.uiState.collectAsState()
    LaunchedEffect(profileStateForLevel.level) {
        val newLevel = profileStateForLevel.level
        if (newLevel < 1) return@LaunchedEffect
        // Skip until the profile has actually loaded — default state has name = ""
        // and level = 1, which would falsely trigger a notification on every launch.
        if (profileStateForLevel.name.isEmpty()) return@LaunchedEffect
        val lastLevel = appPrefs.lastKnownLevel
        if (lastLevel > 0 && newLevel > lastLevel) {
            val title = "¡Subiste de nivel! 🏆"
            val msg   = "Ahora eres nivel $newLevel. ¡Sigue así!"
            NotificationHub.add(title = title, message = msg, type = NotificationType.ACHIEVEMENT)
            SupabaseNotificationRepository.insertForCurrentUser(
                type  = "LEVEL_UP",
                title = title,
                body  = msg
            )
        }
        appPrefs.lastKnownLevel = newLevel
    }

    // Cache today's pending task count so DailyNotificationsReceiver can read it.
    val calendarStateForCache by calendarVM.uiState.collectAsState()
    LaunchedEffect(calendarStateForCache.tasks) {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
        val pending = calendarStateForCache.tasks.count { !it.isDone }
        appPrefs.pendingTaskCount     = pending
        appPrefs.pendingTaskCountDate = today
    }

    // Session restoration: on startup, skip AUTH if a saved session exists;
    // on logout, return to AUTH from wherever the user is.
    val sessionState by sessionVM.state.collectAsState()
    LaunchedEffect(sessionState) {
        val current = navController.currentBackStackEntry?.destination?.route
        when (sessionState) {
            AppSessionViewModel.State.DataLoading -> {
                // Refresh all data in parallel while the LoadingScreen is still visible.
                // joinAll() waits for every Job to complete before marking data as ready.
                // There is no offline queue to flush first — all Supabase writes in this
                // app are fire-and-immediate, so the fresh load below is always authoritative.
                listOf(
                    homeVM.refresh(),
                    calendarVM.refresh(),
                    profileVM.refresh(),
                    progressVM.refresh(),
                    friendsVM.refresh(),
                    rivalryVM.refresh(),
                    settingsVM.refresh(),
                    notificationsVM.load(),
                ).joinAll()
                // Schedule or cancel daily alarms based on current settings.
                val s = settingsVM.uiState.value
                DailyNotificationScheduler.scheduleAll(
                    context              = context,
                    notificationsEnabled = s.notificationsEnabled,
                    taskOn               = s.taskRemindersEnabled,
                    streakOn             = s.streakAlertsEnabled
                )
                sessionVM.markDataLoaded()
            }
            AppSessionViewModel.State.Authenticated -> {
                // Data is already loaded; just navigate to HOME.
                if (current == DayPilotDestinations.LOADING ||
                    current == DayPilotDestinations.AUTH   ||
                    current == null) {
                    navController.navigate(DayPilotDestinations.HOME) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            AppSessionViewModel.State.Unauthenticated -> {
                // From LOADING go to AUTH; also handles logout from any screen.
                if (current != null && current != DayPilotDestinations.AUTH) {
                    navController.navigate(DayPilotDestinations.AUTH) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
            AppSessionViewModel.State.Loading -> { /* show LoadingScreen, wait */ }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute != DayPilotDestinations.LOADING &&
                currentRoute != DayPilotDestinations.AUTH &&
                currentRoute != DayPilotDestinations.RESET_PASSWORD) {
                val notifState by notificationsVM.uiState.collectAsState()
                DayPilotBottomBar(
                    navController        = navController,
                    unreadNotifications  = notifState.unreadCount
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = navController,
            startDestination = DayPilotDestinations.LOADING,
            modifier         = Modifier.padding(innerPadding)
        ) {

            // ── Loading ───────────────────────────────────────────
            composable(DayPilotDestinations.LOADING) {
                LoadingScreen()
            }

            // ── Auth ─────────────────────────────────────────────
            composable(DayPilotDestinations.AUTH) {
                val authState by authVM.uiState.collectAsState()
                AuthScreen(
                    onLoginSuccess      = {},
                    isLoginLoading      = authState.loginLoading,
                    isRegisterLoading   = authState.registerLoading,
                    loginError          = authState.loginError,
                    registerError       = authState.registerError,
                    onLoginClick        = { email, password ->
                        authVM.login(email, password) {
                            // notifyAuthenticated triggers the LaunchedEffect above,
                            // which navigates to HOME and loads all data.
                            sessionVM.notifyAuthenticated()
                        }
                    },
                    onRegisterClick     = { name, username, email, password, region ->
                        authVM.register(name, username, email, password, region) {
                            sessionVM.notifyAuthenticated()
                        }
                    },
                    onForgotPassword    = {
                        navController.navigate(DayPilotDestinations.RESET_PASSWORD)
                    }
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
                    friendCount         = s.friendCount,
                    timerCompletedToday = s.timerCompletedToday,
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
                    friends             = s.friends,
                    friendRequests      = s.friendRequests,
                    acceptingUserId     = s.acceptingUserId,
                    justAcceptedRequest = s.justAcceptedRequest,
                    onAcceptedNavigated = { friendsVM.clearJustAccepted() },
                    onAcceptRequest     = { userId ->
                        friendsVM.acceptRequest(userId)
                        rivalryVM.refresh()
                        homeVM.refresh()
                    },
                    onRejectRequest     = friendsVM::rejectRequest,
                    onTapFriend         = {},
                    onRemoveFriend      = { userId ->
                        friendsVM.removeFriend(userId)
                        rivalryVM.refresh()
                        homeVM.refresh()
                    },
                    onReactToFriend     = friendsVM::reactToFriend,
                    onNavigateToSearch  = { navController.navigate(DayPilotDestinations.SEARCH_FRIENDS) }
                )
            }

            composable(DayPilotDestinations.SEARCH_FRIENDS) {
                val s by searchVM.uiState.collectAsState()
                SearchFriendsScreen(
                    searchResults        = s.searchResults,
                    isLoading            = s.isLoading,
                    requestJustSent      = s.requestJustSent,
                    onSearch             = searchVM::search,
                    onAddFriend          = searchVM::addFriend,
                    onConfirmationDismissed = {
                        searchVM.dismissConfirmation()
                        friendsVM.refresh()
                        rivalryVM.refresh()
                        homeVM.refresh()
                        // Navigate to a fresh Friends screen (tab 0) and clear search from the stack.
                        navController.navigate(DayPilotDestinations.FRIENDS) {
                            popUpTo(DayPilotDestinations.FRIENDS) { inclusive = true }
                        }
                    },
                    onBack               = { navController.popBackStack() }
                )
            }

            // ── Notifications ─────────────────────────────────────
            composable(DayPilotDestinations.NOTIFICATIONS) {
                val s by notificationsVM.uiState.collectAsState()
                LaunchedEffect(Unit) {
                    notificationsVM.markAllAsRead()
                }
                NotificationsScreen(
                    notifications    = s.notifications,
                    onTapNotification= notificationsVM::markAsRead,
                    onMarkAllAsRead  = notificationsVM::markAllAsRead,
                    onBack           = { navController.popBackStack() }
                )
            }

            // ── Profile ───────────────────────────────────────────
            composable(DayPilotDestinations.PROFILE) {
                LaunchedEffect(Unit) { profileVM.refresh() }
                val s by profileVM.uiState.collectAsState()
                ProfileScreen(
                    name                = s.name,
                    username            = s.username,
                    email               = s.email,
                    memberSince         = s.memberSince,
                    level               = s.level,
                    totalPoints         = s.totalPoints,
                    currentStreak       = s.currentStreak,
                    longestStreak       = s.longestStreak,
                    rankingPosition     = s.rankingPosition,
                    pointsToday         = s.pointsToday,
                    stepsToday          = s.stepsToday,
                    tasksCompletedToday = s.tasksCompletedToday,
                    avatarUrl           = s.avatarUrl,
                    weeklySummary       = s.weeklySummary,
                    onNavigateToSettings = { navController.navigate(DayPilotDestinations.SETTINGS) }
                )
            }

            // ── Settings ──────────────────────────────────────────
            composable(DayPilotDestinations.SETTINGS) {
                val s by settingsVM.uiState.collectAsState()
                SettingsScreen(
                    name                    = s.name,
                    isDarkMode              = s.isDarkMode,
                    selectedThemeId         = s.selectedThemeId,
                    selectedLanguage        = s.selectedLanguage,
                    notificationsEnabled    = s.notificationsEnabled,
                    taskRemindersEnabled    = s.taskRemindersEnabled,
                    streakAlertsEnabled     = s.streakAlertsEnabled,
                    onToggleDarkMode        = settingsVM::toggleDarkMode,
                    onThemeSelect           = settingsVM::selectTheme,
                    onLanguageSelect        = { code ->
                        settingsVM.selectLanguage(code)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.getSystemService(LocaleManager::class.java)
                                .applicationLocales = LocaleList.forLanguageTags(code)
                        }
                    },
                    onToggleNotifications   = settingsVM::toggleNotifications,
                    onToggleTaskReminders   = settingsVM::toggleTaskReminders,
                    onToggleStreakAlerts    = settingsVM::toggleStreakAlerts,
                    onNavigateToEditProfile = { navController.navigate(DayPilotDestinations.EDIT_PROFILE) },
                    onLogout                = {
                        techHealthVM.clearLocalData()
                        sessionVM.signOut()
                    },
                    onBack                  = { navController.popBackStack() }
                )
            }

            composable(DayPilotDestinations.EDIT_PROFILE) {
                val s by profileVM.uiState.collectAsState()
                EditProfileScreen(
                    currentName          = s.name,
                    currentUsername      = s.username,
                    avatarUrl            = s.avatarUrl,
                    isUploadingAvatar    = s.isUploadingAvatar,
                    avatarUploadError    = s.avatarUploadError,
                    onSave               = { name, username, region ->
                        profileVM.updateProfile(name, username, region)
                        navController.popBackStack()
                    },
                    onNavigateToResetPassword = {
                        navController.navigate(DayPilotDestinations.RESET_PASSWORD)
                    },
                    onPhotoSelected      = { uri -> profileVM.uploadAvatar(uri, context) },
                    onAvatarErrorDismissed = { profileVM.clearAvatarError() },
                    onBack               = { navController.popBackStack() }
                )
            }

            composable(DayPilotDestinations.RESET_PASSWORD) {
                val authState by authVM.uiState.collectAsState()
                LaunchedEffect(Unit) { authVM.clearResetState() }
                ResetPasswordScreen(
                    isLoading        = authState.resetLoading,
                    isSuccess        = authState.resetSent,
                    errorMessage     = authState.resetError,
                    onSendResetEmail = authVM::sendResetEmail,
                    onBack           = { navController.popBackStack() }
                )
            }

            // ── Calendar ──────────────────────────────────────────
            composable(DayPilotDestinations.CALENDAR) {
                val s by calendarVM.uiState.collectAsState()
                CalendarScreen(
                    tasks         = s.tasks,
                    isProcessing  = s.isProcessing,
                    onBack        = { homeVM.refresh(); navController.popBackStack() },
                    onCreateTask  = calendarVM::addTask,
                    onTapTask     = {},
                    onToggleTask  = { id, isDone ->
                        calendarVM.toggleTask(id, isDone)
                        progressVM.refresh()
                        profileVM.refresh()
                        homeVM.refresh()
                    },
                    onDeleteTask  = { id ->
                        calendarVM.deleteTask(id)
                        progressVM.refresh()
                        profileVM.refresh()
                        homeVM.refresh()
                    },
                    onEditTask    = calendarVM::editTask,
                    onUpdateTask  = calendarVM::updateTask
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
                    goalChangedToday      = s.goalChangedToday,
                    pendingGoal           = s.pendingGoal,
                    onBack                = { navController.popBackStack() },
                    onNavigateToTimer     = { navController.navigate(DayPilotDestinations.TIMER_HUB) },
                    onNavigateToReminders = { navController.navigate(DayPilotDestinations.REMINDERS) },
                    onNavigateToTechHealth= { navController.navigate(DayPilotDestinations.TECH_HEALTH) },
                    onConfigureGoal       = { newGoal ->
                        habitsVM.configureGoal(newGoal)
                        stepsVM.configureGoal(newGoal)
                    }
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
                LaunchedEffect(Unit) { progressVM.refresh() }
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
                LaunchedEffect(Unit) { rivalryVM.refresh() }
                val s by rivalryVM.uiState.collectAsState()
                RivalryScreen(
                    currentUserName     = s.currentUserName,
                    currentUserId       = s.currentUserId,
                    currentUserPosition = s.currentUserPosition,
                    currentUserPoints   = s.currentUserPoints,
                    currentUserStreak   = s.currentUserStreak,
                    currentUserLevel    = s.currentUserLevel,
                    ranking             = s.ranking,
                    onBack              = { navController.popBackStack() }
                )
            }

            // ── Steps ────────────────────────────────────────────
            composable(DayPilotDestinations.STEPS) {
                val s by stepsVM.uiState.collectAsState()
                StepsScreen(
                    currentSteps     = s.currentSteps,
                    goalSteps        = s.goalSteps,
                    pointsEarned     = s.pointsEarned,
                    pointsRemaining  = s.pointsRemaining,
                    totalSteps7Days  = s.totalSteps7Days,
                    bestDaySteps     = s.bestDaySteps,
                    dailyAverage     = s.dailyAverage,
                    goalStreak       = s.goalStreak,
                    pendingGoal      = s.pendingGoal,
                    goalChangedToday = s.goalChangedToday,
                    sensorAvailable  = s.sensorAvailable,
                    onBack           = { habitsVM.refresh(); navController.popBackStack() },
                    onConfigureGoal  = stepsVM::configureGoal
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
                val ps by progressVM.uiState.collectAsState()
                TimerScreen(
                    timerMode        = mode,
                    customMinutes    = minutes,
                    pointEarnedToday = ps.timerCompletedToday,
                    onTimerCompleted = {
                        progressVM.recordTimerComplete()
                        homeVM.refresh()
                    },
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
                LaunchedEffect(Unit) { techHealthVM.refreshUsage() }
                val s by techHealthVM.uiState.collectAsState()
                TechHealthScreen(
                    appRestrictions        = s.appRestrictions,
                    groupRestrictions      = s.groupRestrictions,
                    hasUsagePermission     = s.hasUsagePermission,
                    techHealthPointEarned  = s.techHealthPointEarned,
                    activeRestrictionCount = s.activeRestrictionCount,
                    onSaveApp              = { restriction, _ -> techHealthVM.saveApp(restriction) },
                    onSaveGroup            = { group, _       -> techHealthVM.saveGroup(group) },
                    onToggleRestriction    = techHealthVM::toggleRestriction,
                    onDeleteRestriction    = techHealthVM::deleteRestriction,
                    onToggleGroup          = techHealthVM::toggleGroup,
                    onDeleteGroup          = techHealthVM::deleteGroup,
                    onBack                 = { navController.popBackStack() }
                )
            }
        }
    }
}
