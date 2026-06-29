package com.example.daypilot_test_desing

import com.example.daypilot_test_desing.ui.components.basic.DayPilotReactionBar
import com.example.daypilot_test_desing.ui.components.basic.DayPilotReactionSummary
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.daypilot_test_desing.ui.components.DayPilotCalendar
import com.example.daypilot_test_desing.ui.components.basic.*
import com.example.daypilot_test_desing.ui.components.cards.*
import com.example.daypilot_test_desing.ui.components.forms.*
import com.example.daypilot_test_desing.backend.model.*
import com.example.daypilot_test_desing.ui.theme.DayPilotTheme

// ── Preview 1: Botones, Avatar, Chips, TextFields ─────────────────
@Preview(showBackground = true)
@Composable
fun CatalogBasicInputs() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Buttons") {
                DayPilotButtonPrimary(text = "Primary",  onClick = {})
                DayPilotButtonOutlined(text = "Outlined", onClick = {})
                DayPilotButtonError(text = "Error",       onClick = {})
                DayPilotButtonText(text = "Text button",  onClick = {})
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayPilotFAB(icon = Icons.Default.Add,  onClick = {})
                    DayPilotIconButton(icon = Icons.Default.Edit, onClick = {})
                }
            }
            CatalogSection("Avatar") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DayPilotAvatar(name = "Mario García", size = 40)
                    DayPilotAvatar(name = "Ana López",    size = 56)
                    DayPilotAvatar(name = "Carlos Ruiz",  size = 72)
                }
            }
            CatalogSection("Chips") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DifficultyChip(difficulty = TaskDifficulty.EASY)
                    DifficultyChip(difficulty = TaskDifficulty.MEDIUM)
                    DifficultyChip(difficulty = TaskDifficulty.HARD)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CategoryChip(category = TaskCategory.WORK)
                    CategoryChip(category = TaskCategory.SPORT)
                    CategoryChip(category = TaskCategory.STUDY)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DurationChip(minutes = 30)
                    DurationChip(minutes = 60)
                    DurationChip(minutes = 90)
                }
            }
            CatalogSection("TextFields") {
                var text by remember { mutableStateOf("") }
                var pass  by remember { mutableStateOf("") }
                DayPilotTextField(value = text, onValueChange = { text = it }, label = "Name", leadingIcon = Icons.Default.Person)
                DayPilotPasswordField(value = pass, onValueChange = { pass = it })
                DayPilotTextField(value = "Error field", onValueChange = {}, label = "Email", isError = true, errorMessage = "Invalid email")
            }
        }
    }
}

// ── Preview 2: TopBars, SectionHeaders, Divider, EmptyState, Switch, Stats ────
@Preview(showBackground = true)
@Composable
fun CatalogBasicLayout() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("TopBars") {
                DayPilotTopBar(title = "Simple TopBar")
                DayPilotTopBar(title = "With Back", onBack = {})
                DayPilotTopBarWithAction(title = "With Action", actionIcon = Icons.Default.Settings, onAction = {})
            }
            CatalogSection("Section Header") {
                DayPilotSectionHeader(title = "Section title")
                DayPilotSectionHeader(title = "With action", actionText = "See all", onAction = {})
            }
            CatalogSection("Divider") {
                DayPilotDivider()
                DayPilotDivider(horizontalPadding = 16.dp)
            }
            CatalogSection("Empty State") {
                DayPilotEmptyState(message = "No items found", icon = Icons.Default.SearchOff, modifier = Modifier.height(120.dp))
            }
            CatalogSection("Switch Row") {
                var checked by remember { mutableStateOf(true) }
                DayPilotSwitchRow(title = "Dark mode", description = "Enable dark theme", icon = Icons.Default.DarkMode, checked = checked, onCheckedChange = { checked = it })
            }
            CatalogSection("Stats & Weekly") {
                DayPilotStatItem(label = "Points", value = "340")
                DayPilotStatsRow(points = 340, streak = 7)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DayPilotWeeklyStat(emoji = "⭐", value = "45",  label = "pts")
                    DayPilotWeeklyStat(emoji = "✅", value = "12",  label = "tasks")
                    DayPilotWeeklyStat(emoji = "👣", value = "42k", label = "steps")
                }
            }
        }
    }
}

// ── Preview 3: Reactions, Selectors, TaskDot, Steps, Profile blocks ───────────
@Preview(showBackground = true)
@Composable
fun CatalogBasicMisc() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Reactions") {
                var selected by remember { mutableStateOf<ReactionType?>(null) }
                DayPilotReactionBar(selectedReaction = selected, onReact = { selected = it })
                DayPilotReactionSummary(reactions = mapOf(ReactionType.FIRE to 3, ReactionType.CLAP to 1, ReactionType.STRONG to 5))
                DayPilotReactionBadgeRow(reactions = listOf(ReceivedReaction("Ana", ReactionType.CLAP), ReceivedReaction("Carlos", ReactionType.FIRE)))
            }
            CatalogSection("Filter Selector") {
                var selected by remember { mutableStateOf(FilterOption("all", "All")) }
                DayPilotFilterSelector(
                    selectedOption = selected,
                    options = listOf(FilterOption("all", "All"), FilterOption("easy", "Easy"), FilterOption("hard", "Hard")),
                    onSelect = { selected = it }
                )
            }
            CatalogSection("Task Dot, Steps, Profile blocks") {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TaskDot(color = Color(0xFF4CAF50))
                    TaskDot(color = Color(0xFFFF9800))
                    TaskDot(color = Color(0xFFF44336))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MilestoneChip(label = "50%",  reached = true)
                    MilestoneChip(label = "75%",  reached = true)
                    MilestoneChip(label = "100%", reached = false)
                }
                StepStatRow(label = "Points earned today", value = "2 pts")
                StatsTopBlock(icon = Icons.Default.EmojiEvents, label = "Ranking", value = "#2")
                StatsBreakdownRow(icon = Icons.Default.CheckCircle, label = "Tasks", points = 4)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryStatCard(label = "Total 7 days", value = "42000", modifier = Modifier.weight(1f))
                    SummaryStatCard(label = "Best day",     value = "8500",  modifier = Modifier.weight(1f))
                }
                DailySummaryStat(icon = Icons.AutoMirrored.Filled.DirectionsWalk, label = "Steps", value = "1200", subValue = "goal 2000")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProfileStatBlock(icon = Icons.Default.Star,     label = "Total points", value = "340",  modifier = Modifier.weight(1f))
                    ProfileStatBlock(icon = Icons.Default.Whatshot, label = "Streak",       value = "7🔥", modifier = Modifier.weight(1f))
                }
            }
            CatalogSection("Selectors") {
                var themeId  by remember { mutableStateOf("sage_green") }
                var darkMode by remember { mutableStateOf(true) }
                var language by remember { mutableStateOf("English") }
                DayPilotThemeSelector(selectedThemeId = themeId, onThemeSelect = { themeId = it })
                DayPilotDarkModeSelector(isDarkMode = darkMode, onToggle = { darkMode = it })
                DayPilotOptionSelector(title = "Language", icon = Icons.Default.Language, selectedOption = language, options = listOf("English", "Español", "Deutsch"), onSelect = { language = it })
            }
        }
    }
}

// ── Preview 4: Task Cards, Notification, Habit, Friend, User cards ────────────
@Preview(showBackground = true)
@Composable
fun CatalogCardsPeople() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Task Cards") {
                TaskCard(title = "Finish TFG", category = TaskCategory.STUDY, difficulty = TaskDifficulty.HARD, durationMinutes = 120, isCompleted = false, onToggleComplete = {}, onTap = {})
                TaskCard(title = "Go for a run", category = TaskCategory.SPORT, difficulty = TaskDifficulty.EASY, durationMinutes = 45, isCompleted = true, onToggleComplete = {}, onTap = {})
                TaskMiniCard(title = "Team meeting", difficulty = TaskDifficulty.MEDIUM, onTap = {})
                TaskDayCard(title = "Presentation", category = TaskCategory.WORK, difficulty = TaskDifficulty.HARD, durationMinutes = 60, isCompleted = false, onToggleComplete = {}, onTap = {}, onEdit = {}, onDelete = {})
            }
            CatalogSection("Notification Cards") {
                NotificationCard(title = "Task completed!", message = "You completed 'Go for a run'", timeAgo = "5min ago", type = NotificationType.TASK, isRead = false, onClick = {})
                NotificationCard(title = "New request", message = "Ana López wants to be your friend", timeAgo = "1h ago", type = NotificationType.SOCIAL, isRead = true, onClick = {})
            }
            CatalogSection("Habit & Friend Cards") {
                HabitCard(title = "Tech health", description = "App / group limits",  icon = Icons.Default.PhoneAndroid,  onClick = {})
                HabitCard(title = "Reminders",   description = "Alerts and routines", icon = Icons.Default.Notifications, onClick = {})
                FriendCard(name = "Ana López",   email = "ana@example.com",    points = 520, streak = 14)
                FriendCard(name = "Carlos Ruiz", email = "carlos@example.com", points = 480, streak = 9,
                    weeklySummary = FriendWeeklySummary(totalPoints = 45, tasksCompleted = 12, totalSteps = 42000, bestStreak = 7))
                UserSearchCard(name = "Mario García", email = "mario@example.com", points = 340, streak = 7, onAddFriend = {})
                FriendRequestCard(name = "Ana López", email = "ana@example.com", points = 210, streak = 3, onAccept = {}, onReject = {})
            }
        }
    }
}

// ── Preview 5: Ranking, Podium, Reminder, Timer cards ─────────────────────────
@Preview(showBackground = true)
@Composable
fun CatalogCardsRankingTimer() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Ranking & Podium") {
                RankingCard(name = "Ana López",   position = 1, points = 520, streak = 14)
                RankingCard(name = "Carlos Ruiz", position = 2, points = 480, streak = 9)
                CurrentUserRankingCard(name = "Mario García", position = 3, points = 340, streak = 7)
                PodiumCard(
                    first  = PodiumEntry("Ana López",     520, 14),
                    second = PodiumEntry("Carlos Ruiz",   480, 9),
                    third  = PodiumEntry("Laura Sánchez", 430, 6)
                )
            }
            CatalogSection("Reminder & Timer Cards") {
                ReminderCard(title = "Morning exercise", time = "07:30", isEnabled = true,  onToggle = {}, onDelete = {})
                ReminderCard(title = "Meditation",       time = "22:00", isEnabled = false, onToggle = {}, onDelete = {})
                TimerCard(mode = TimerMode.POMODORO,  pointEarnedToday = true,  onStart = {})
                TimerCard(mode = TimerMode.TRAINING,  pointEarnedToday = false, onStart = {})
                TimerCard(mode = TimerMode.CUSTOM,    pointEarnedToday = false, onStart = {}, customMinutes = 45)
                TimerHubCard(
                    timer = TimerOption(
                        id             = "POMODORO",
                        labelRes       = R.string.timer_pomodoro,
                        descriptionRes = R.string.timer_pomodoro_desc,
                        icon           = Icons.Default.Timer,
                        accentColor    = Color(0xFFE53935),
                        isPomodoro     = true
                    ),
                    onClick = {}
                )
            }
        }
    }
}

// ── Preview 6: Stats, Profile, Weekly, Daily, Steps, AppLimit cards ───────────
@Preview(showBackground = true)
@Composable
fun CatalogCardsStats() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Stats & Profile Cards") {
                StatsCard(rankingPosition = 2, pointsToday = 8, pointsFromTasks = 4, pointsFromSteps = 2, pointsFromHabits = 1, pointsFromTimers = 1)
                ProfileStatsCard(name = "Mario García", username = "mariogarcia", level = 7, totalPoints = 340, currentStreak = 7, longestStreak = 14)
                ProfileInfoRow(label = "Email",    value = "mario@example.com")
                ProfileInfoRow(label = "Username", value = "@mariogarcia")
            }
            CatalogSection("Weekly & Daily Summary") {
                WeeklyReactionCard(
                    summary = WeeklySummaryData(totalPoints = 45, tasksCompleted = 12, totalSteps = 42000, bestStreak = 7,
                        reactions = listOf(ReceivedReaction("Ana", ReactionType.CLAP), ReceivedReaction("Carlos", ReactionType.FIRE)))
                )
                DailySummaryCard(userName = "Mario", streak = 7, stepsToday = 1200, stepsGoal = 2000, tasksCompleted = 3, tasksTotal = 5, pointsToday = 8, rankingPosition = 2)
            }
            CatalogSection("Steps & App Limit Cards") {
                StepsCard(currentSteps = 1200, goalSteps = 2000, pointsEarned = 1, pointsRemaining = 5, onConfigureGoal = { _ -> })
                StepsSummaryCard(totalSteps7Days = 42000, bestDaySteps = 8500, dailyAverage = 6000, goalStreak = 4)
                AppLimitCard(
                    restriction = AppRestriction(id = "1", appName = "YouTube", packageName = "com.google.youtube", dailyLimitMinutes = 120, notificationIntervalSeconds = 60, isEnabled = true, usedMinutesToday = 45),
                    onToggle = {}, onEdit = {}, onDelete = {}
                )
                GroupLimitCard(
                    restriction = GroupRestriction(id = "1", groupName = "Social Networks",
                        apps = listOf(AppRestriction("1", "Instagram", "com.instagram.android", 60, 30, true, 20)),
                        dailyLimitMinutes = 120, notificationIntervalSeconds = 60, isEnabled = true, usedMinutesToday = 45),
                    onToggle = {}, onEdit = {}, onDelete = {}
                )
            }
        }
    }
}

// ── Preview 7: Progress Chart, Home Menu, Calendar ─────────────────────────────
@Preview(showBackground = true)
@Composable
fun CatalogCardsCalendar() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Progress Chart Card") {
                ProgressChartCard(
                    data = List(30) { i -> DayProgress(i + 1, (5..25).random(), (500..3000).random(), (0..8).random()) },
                    filter = ProgressFilter.POINTS
                )
            }
            CatalogSection("Home Menu Cards") {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeMenuCard(section = HomeSection.CALENDAR, data = HomeSectionData.Calendar(pendingTasks = 2, completedTasks = 3), onClick = {}, modifier = Modifier.weight(1f))
                    HomeMenuCard(section = HomeSection.RIVALRY,  data = HomeSectionData.Rivalry(position = 2, totalFriends = 5),        onClick = {}, modifier = Modifier.weight(1f))
                }
            }
            CatalogSection("Calendar") {
                DayPilotCalendar(
                    month = 5, year = 2026,
                    taskDots = listOf(
                        CalendarTaskDot(day = 5,  month = 5, year = 2026, color = Color(0xFF4CAF50)),
                        CalendarTaskDot(day = 10, month = 5, year = 2026, color = Color(0xFFFF9800)),
                        CalendarTaskDot(day = 15, month = 5, year = 2026, color = Color(0xFFF44336))
                    ),
                    selectedDay     = 5,
                    onDaySelected   = {},
                    onPreviousMonth = {},
                    onNextMonth     = {},
                    onAddTask       = {}
                )
            }
        }
    }
}

// ── Preview 8: Forms — Task, Reminder, AppLimit, Auth ─────────────────────────
@Preview(showBackground = true)
@Composable
fun CatalogForms() {
    DayPilotTheme(theme = DayPilotTheme.SAGE_GREEN, darkMode = true) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            CatalogSection("Task Form") {
                TaskFormCard(onSave = { _, _, _, _, _, _, _, _ -> }, onCancel = {})
            }
            CatalogSection("Reminder Form") {
                ReminderFormCard(onSave = {}, onCancel = {})
            }
            CatalogSection("App Limit Form") {
                AppLimitFormCard(onSaveApp = {}, onSaveGroup = {}, onCancel = {})
            }
            CatalogSection("Auth Components") {
                AuthToggle(isLogin = true, onToggle = {})
                LoginCard(onLogin = { _, _ -> })
            }
        }
    }
}

// ── Catalog section wrapper ───────────────────────────────────────
@Composable
private fun CatalogSection(
    title  : String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), content = content)
    }
}