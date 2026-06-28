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
import com.example.daypilot_test_desing.backend.supabase.SupabaseFriendRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseProgressRepository
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
    val rivalryVM: RivalryViewModel             = viewModel()
    val remindersVM: RemindersViewModel         = viewModel()
    val techHealthVM: TechHealthViewModel       = viewModel()

    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Shared repository instances — created once, passed to all ViewModels that need them.
    val stepsRepo    = remember { SupabaseStepsRepository(application.getSharedPreferences("daypilot_steps", Context.MODE_PRIVATE)) }
    val progressRepo = remember { SupabaseProgressRepository() }
    val userRepo     = remember { SupabaseUserRepository() }
    val friendRepo   = remember { SupabaseFriendRepository() }

    val homeVM: HomeViewModel     = viewModel(factory = HomeViewModel.factory(stepsRepo, progressRepo, userRepo, friendRepo))
    val friendsVM: FriendsViewModel     = viewModel(factory = FriendsViewModel.factory(friendRepo))
    val searchVM: SearchFriendsViewModel = viewModel(factory = SearchFriendsViewModel.factory(friendRepo))
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

    // Session restoration: on startup, skip AUTH if a saved session exists;
    // on logout, return to AUTH from wherever the user is.
    val sessionState by sessionVM.state.collectAsState()
    LaunchedEffect(sessionState) {
        val current = navController.currentBackStackEntry?.destination?.route
        when (sessionState) {
            AppSessionViewModel.State.Authenticated -> {
                sessionVM.loadAll(calendarVM::refresh, homeVM::refresh)
                // From LOADING or AUTH go to HOME; never navigate if already inside the app.
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
                DayPilotBottomBar(navController = navController)
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
                    userName              = s.userName,
                    streak                = s.streak,
                    stepsToday            = s.stepsToday,
                    stepsGoal             = s.stepsGoal,
                    tasksCompleted        = s.tasksCompleted,
                    tasksTotal            = s.tasksTotal,
                    progressData          = s.progressData,
                    pointsToday           = s.pointsToday,
                    rankingPosition       = s.rankingPosition,
                    friendCount           = s.friendCount,
                    timerCompletedToday   = s.timerCompletedToday,
                    onNavigateToCalendar  = { navController.navigate(DayPilotDestinations.CALENDAR) },
                    onNavigateToHabits    = { navController.navigate(DayPilotDestinations.HABITS) },
                    onNavigateToProgress  = { navController.navigate(DayPilotDestinations.PROGRESS) },
                    onNavigateToRivalry   = { navController.navigate(DayPilotDestinations.RIVALRY) }
                )
            }

            // ── Friends ──────────────────────────────────────────
            composable(DayPilotDestinations.FRIENDS) {
                val s by friendsVM.uiState.collectAsState()
                FriendsScreen(
                    friends            = s.friends,
                    friendRequests     = s.friendRequests,
                    onAcceptRequest    = { userId ->
                        friendsVM.acceptRequest(userId)
                        rivalryVM.refresh()
                        homeVM.refresh()
                    },
                    onRejectRequest    = friendsVM::rejectRequest,
                    onTapFriend        = {},
                    onRemoveFriend     = { userId ->
                        friendsVM.removeFriend(userId)
                        rivalryVM.refresh()
                        homeVM.refresh()
                    },
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
                        rivalryVM.refresh()
                        homeVM.refresh()
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
                    onLanguageSelect     = { code ->
                        settingsVM.selectLanguage(code)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            context.getSystemService(LocaleManager::class.java)
                                .applicationLocales = LocaleList.forLanguageTags(code)
                        }
                    },
                    onToggleNotifications= settingsVM::toggleNotifications,
                    onNavigateToEditProfile = { navController.navigate(DayPilotDestinations.EDIT_PROFILE) },
                    onLogout = {
                        // signOut() signs out of Supabase and flips the session
                        // state to Unauthenticated; the LaunchedEffect handles navigation.
                        sessionVM.signOut()
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
                    onNavigateToSteps     = { navController.navigate(DayPilotDestinations.STEPS) },
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
