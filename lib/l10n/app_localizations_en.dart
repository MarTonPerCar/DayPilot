// ignore: unused_import
import 'package:intl/intl.dart' as intl;
import 'app_localizations.dart';

// ignore_for_file: type=lint

/// The translations for English (`en`).
class AppLocalizationsEn extends AppLocalizations {
  AppLocalizationsEn([String locale = 'en']) : super(locale);

  @override
  String get navInicio => 'Home';

  @override
  String get navAmigos => 'Friends';

  @override
  String get navAvisos => 'Alerts';

  @override
  String get navPerfil => 'Profile';

  @override
  String get commonCancel => 'Cancel';

  @override
  String get commonSave => 'Save';

  @override
  String get commonSaveChanges => 'Save changes';

  @override
  String get commonCreate => 'Create';

  @override
  String get commonAdd => 'Add';

  @override
  String get commonEdit => 'Edit';

  @override
  String get commonDelete => 'Delete';

  @override
  String get commonStart => 'Start';

  @override
  String get commonAccept => 'Accept';

  @override
  String get commonYes => 'Yes';

  @override
  String get commonNo => 'No';

  @override
  String get commonToday => 'Today';

  @override
  String get commonTomorrow => 'Tomorrow';

  @override
  String get commonSeeAll => 'See all';

  @override
  String get commonOr => 'or';

  @override
  String get commonPassword => 'Password';

  @override
  String get commonSteps => 'Steps';

  @override
  String get commonTasks => 'Tasks';

  @override
  String get commonHabits => 'Habits';

  @override
  String get commonTimer => 'Timer';

  @override
  String get commonPoints => 'Points';

  @override
  String get commonPointsToday => 'Points today';

  @override
  String get commonRanking => 'Ranking';

  @override
  String get commonTotal => 'Total';

  @override
  String get commonAverage => 'Average';

  @override
  String get commonBest => 'Best';

  @override
  String commonPointsSuffix(int points) {
    return '$points pts';
  }

  @override
  String get settingsTitle => 'Settings';

  @override
  String get settingsColorTheme => 'Color theme';

  @override
  String get settingsDarkMode => 'Dark mode';

  @override
  String get settingsDarkModeSubtitle => 'Turn on the app\'s dark theme';

  @override
  String get settingsNotifications => 'Notifications';

  @override
  String get settingsNotificationsSubtitle => 'Turn notifications on or off';

  @override
  String get settingsTaskReminders => 'Task reminders';

  @override
  String get settingsTaskRemindersSubtitle =>
      'Daily alert with your pending tasks';

  @override
  String get settingsStreakAlert => 'Streak alert';

  @override
  String get settingsStreakAlertSubtitle =>
      'Alert at 10pm if you haven\'t opened the app';

  @override
  String get settingsLaunchAtStartup => 'Launch at startup';

  @override
  String get settingsLaunchAtStartupSubtitle =>
      'Open DayPilot automatically when you sign in to your PC';

  @override
  String get settingsEditProfile => 'Edit profile';

  @override
  String get settingsEditProfileSubtitle =>
      'Name, username, region and password';

  @override
  String get settingsChangePhoto => 'Change photo';

  @override
  String get settingsChangePhotoPrompt =>
      'Where do you want to choose the photo from?';

  @override
  String get settingsGallery => 'Gallery';

  @override
  String get settingsCamera => 'Camera';

  @override
  String get settingsAdjustPhoto => 'Adjust photo';

  @override
  String get settingsPersonalInfo => 'Personal information';

  @override
  String get settingsSecurity => 'Security';

  @override
  String get settingsChangePassword => 'Change password';

  @override
  String get settingsNewPassword => 'New password';

  @override
  String get settingsConfirmPassword => 'Confirm password';

  @override
  String get settingsPasswordMismatch => 'Passwords don\'t match';

  @override
  String get settingsPasswordChanged => 'Password updated';

  @override
  String get settingsAvatarUploadError => 'Couldn\'t upload the photo';

  @override
  String get settingsLanguage => 'Language';

  @override
  String get settingsChooseLanguage => 'Choose a language';

  @override
  String get settingsDeveloper => 'Developer';

  @override
  String get settingsComponentCatalog => 'Component catalog';

  @override
  String get settingsSignOut => 'Sign out';

  @override
  String get themeSageGreen => 'Sage Green';

  @override
  String get themeOcean => 'Ocean';

  @override
  String get themeLavender => 'Lavender';

  @override
  String get themeAmber => 'Amber';

  @override
  String get themeAmoled => 'AMOLED';

  @override
  String get difficultyEasy => 'Easy';

  @override
  String get difficultyMedium => 'Medium';

  @override
  String get difficultyHard => 'Hard';

  @override
  String get categoryTrabajo => 'Work';

  @override
  String get categoryEstudio => 'Study';

  @override
  String get categoryDeporte => 'Sport';

  @override
  String get categorySalud => 'Health';

  @override
  String get categoryPersonal => 'Personal';

  @override
  String get categoryHogar => 'Home';

  @override
  String get categoryOtro => 'Other';

  @override
  String get calendarTitle => 'Calendar';

  @override
  String get calendarDifficulty => 'Difficulty';

  @override
  String get calendarCategory => 'Category';

  @override
  String get calendarAll => 'All';

  @override
  String calendarTasksForDay(int day) {
    return 'Tasks for day $day';
  }

  @override
  String get calendarAdd => 'Add';

  @override
  String get calendarEmptyDay => 'No tasks for this day';

  @override
  String get stepsGoalTitle => 'Set your steps goal';

  @override
  String stepsGoalValue(int goal) {
    return '$goal steps';
  }

  @override
  String get stepsGoalQuickGoals => 'Quick goals';

  @override
  String get timerCustomTitle => 'Custom timer';

  @override
  String timerMinutesValue(int minutes) {
    return '$minutes minutes';
  }

  @override
  String get timerPomodoroConfigTitle => 'Configure Pomodoro';

  @override
  String get timerWork => 'Work';

  @override
  String get timerRest => 'Break';

  @override
  String get timerTotal => 'Total';

  @override
  String get timerSessionsCount => 'Number of sessions';

  @override
  String timerSessionsValue(int n) {
    return '$n sessions';
  }

  @override
  String get timerStartPomodoro => 'Start Pomodoro';

  @override
  String timerOneSession(int min) {
    return '1 session ($min min)';
  }

  @override
  String timerEightSessions(String h) {
    return '8 sessions (${h}h)';
  }

  @override
  String timerMinValue(int min) {
    return '$min min';
  }

  @override
  String get taskEditTitle => 'Edit task';

  @override
  String get taskNewTitle => 'New task';

  @override
  String get taskInfoSection => 'Information';

  @override
  String get taskTitleLabel => 'Title';

  @override
  String get taskTitleRequired => 'The title is required';

  @override
  String get taskDescriptionLabel => 'Description (optional)';

  @override
  String get taskDetailsSection => 'Details';

  @override
  String get taskDurationEstimate => 'Estimated duration';

  @override
  String get taskMinSuffix => 'min';

  @override
  String get taskDaysSuffix => 'days';

  @override
  String get taskReminderSection => 'Reminder and repeat';

  @override
  String get taskActivateReminder => 'Enable reminder';

  @override
  String get taskReminderSubtitle => 'You\'ll get a notification';

  @override
  String get taskRecurring => 'Recurring task';

  @override
  String get taskRecurringSubtitle => 'Repeats every X days';

  @override
  String get taskRepeatEvery => 'Repeat every';

  @override
  String get taskCreateButton => 'Create task';

  @override
  String get taskErrorCreate => 'Couldn\'t create the task.';

  @override
  String get taskErrorUpdate => 'Couldn\'t update the task.';

  @override
  String get taskErrorToggle => 'Couldn\'t update the task.';

  @override
  String get taskErrorDelete => 'Couldn\'t delete the task.';

  @override
  String get techRestrictionTypeApp => 'App';

  @override
  String get techRestrictionTypeGroup => 'Group';

  @override
  String techRestrictionUsageToday(int used, int limit) {
    return 'Today: $used / $limit min';
  }

  @override
  String techRestrictionNotifyEvery(int seconds) {
    return 'Notify: every ${seconds}s';
  }

  @override
  String get techRestrictionDeletesTomorrow => '🗑️  Deletes tomorrow';

  @override
  String get restrictionPickApps => 'Choose apps';

  @override
  String get restrictionPickApp => 'Choose app';

  @override
  String get restrictionDone => 'Done';

  @override
  String get restrictionNewTitle => 'New restriction';

  @override
  String get restrictionApplication => 'Application';

  @override
  String get restrictionGroupName => 'Group name';

  @override
  String get restrictionGroupApps => 'Apps in group';

  @override
  String restrictionAppsSelected(int n) {
    return '$n apps selected';
  }

  @override
  String restrictionDailyLimitRange(String min, String max) {
    return 'Daily limit ($min → $max)';
  }

  @override
  String restrictionSelected(String value) {
    return 'Selected: $value';
  }

  @override
  String get reminderFrequencyOnce => 'Once';

  @override
  String get reminderFrequencyDaily => 'Daily';

  @override
  String get reminderFrequencyWeekly => 'Weekly';

  @override
  String get reminderNewTitle => 'New reminder';

  @override
  String get reminderNameLabel => 'Reminder name';

  @override
  String get reminderQuickAccess => 'Quick access';

  @override
  String get reminderOrPickDateTime => 'or pick a date and time';

  @override
  String get reminderNoDateSelected => 'No date selected';

  @override
  String get reminderFrequency => 'Frequency';

  @override
  String get reminderNotifyBefore => 'Advance notice';

  @override
  String get reminderNotifyBeforeSubtitle => 'Notifies 10 min before';

  @override
  String get reminderCreateButton => 'Create reminder';

  @override
  String get taskDetailReminderActive => 'Reminder on';

  @override
  String get taskDetailMarkPending => 'Mark as pending';

  @override
  String get taskDetailMarkDone => 'Mark as done';

  @override
  String get stepsUnitLower => 'steps';

  @override
  String stepsGoalCaption(String goal) {
    return 'Goal: $goal steps';
  }

  @override
  String stepsCompletedPercent(String percent) {
    return '$percent% complete';
  }

  @override
  String stepsPointsEarned(int points) {
    return '+$points pts';
  }

  @override
  String get stepsThisWeek => 'This week';

  @override
  String stepsAveragePerDay(String avg) {
    return 'Average: $avg/day';
  }

  @override
  String stepsWeekTotal(String total) {
    return 'Total: $total steps';
  }

  @override
  String get dailySummaryTitle => 'Today\'s summary';

  @override
  String get profileInfoEmail => 'Email';

  @override
  String get profileInfoSince => 'Member since';

  @override
  String get profileInfoUsername => 'Username';

  @override
  String profileLevelBadge(int level) {
    return 'Level $level';
  }

  @override
  String profileLevelProgress(int level) {
    return 'Level $level progress';
  }

  @override
  String profileXpProgress(int current, int toNext) {
    return '$current / $toNext pts';
  }

  @override
  String get profileTotalPoints => 'Total points';

  @override
  String get profileCurrentStreak => 'Current streak';

  @override
  String get profileBestStreak => 'Best streak';

  @override
  String get notifTypeSocial => 'Social';

  @override
  String get notifTypeStreak => 'Streak';

  @override
  String get notifTypeReminder => 'Reminders';

  @override
  String get notifTypeAchievement => 'Achievements';

  @override
  String get notifTimeJustNow => 'just now';

  @override
  String notifTimeMinutesAgo(int n) {
    return '$n min ago';
  }

  @override
  String notifTimeHoursAgo(int n) {
    return '$n h ago';
  }

  @override
  String get notifTimeYesterday => 'yesterday';

  @override
  String notifTimeDaysAgo(int n) {
    return '$n days ago';
  }

  @override
  String get notifFriendRequestTitle => 'New friend request';

  @override
  String notifFriendRequestBody(String username) {
    return '$username wants to be your friend.';
  }

  @override
  String get notifFriendAcceptedTitle => 'Request accepted';

  @override
  String notifFriendAcceptedBody(String username) {
    return '$username accepted your friend request.';
  }

  @override
  String get notifReactionTitle => 'New reaction';

  @override
  String notifReactionBody(String username, String emoji) {
    return '$username reacted to your weekly summary with $emoji.';
  }

  @override
  String get notifTimerDoneTitle => 'Timer completed! ⏱';

  @override
  String get notifTimerDoneBody =>
      'You completed a focus session and earned 10 pts.';

  @override
  String get notifLevelUpTitle => 'You leveled up! 🏆';

  @override
  String notifLevelUpBody(int level) {
    return 'You\'re now level $level. Keep it up!';
  }

  @override
  String get notifStepsGoalTitle => 'Goal reached! 🎉';

  @override
  String get notifStepsGoalBody => 'You reached your steps goal (+30 pts).';

  @override
  String get notifTaskCompletedTitle => 'Task completed! ✅';

  @override
  String notifTaskCompletedBody(String title) {
    return 'You completed \"$title\" and earned 20 pts.';
  }

  @override
  String get notifTaskReminderTitle => 'Today\'s Tasks';

  @override
  String notifTaskReminderCount(int count) {
    return 'You have $count tasks due today';
  }

  @override
  String get notifTaskReminderNone => 'No tasks for today ✓';

  @override
  String get notifTaskReminderGeneric => 'Check your tasks for today';

  @override
  String get notifStreakDangerTitle => 'Streak in Danger! 🔥';

  @override
  String get notifStreakDangerBody =>
      'Open the app before midnight to keep your streak';

  @override
  String get timerPause => 'Pause';

  @override
  String get stepsConfigureGoal => 'Set goal';

  @override
  String get stepsMilestones => 'Milestones';

  @override
  String stepsOfGoal(int goal) {
    return 'of $goal';
  }

  @override
  String get stepsPointsEarnedToday => 'Points earned today';

  @override
  String get stepsNextGoal => 'Next milestone';

  @override
  String stepsPendingGoal(int goal) {
    return 'Pending goal: $goal steps (active tomorrow)';
  }

  @override
  String get notificationsMarkAllRead => 'Mark all read';

  @override
  String get notificationsEmpty => 'You have no notifications';

  @override
  String get friendCardRemoveTooltip => 'Remove friend';

  @override
  String get friendCardNoActivity => 'No activity last week';

  @override
  String get friendCardDecline => 'Decline';

  @override
  String get friendCardPending => 'Pending';

  @override
  String dailySummaryGreeting(String name) {
    return 'Hi, $name 👋';
  }

  @override
  String get dailySummarySubtitle => 'Your summary for today';

  @override
  String dailySummaryStreakDays(int days) {
    return '$days days';
  }

  @override
  String dailySummaryGoalCaption(String goal) {
    return 'goal $goal';
  }

  @override
  String get dailySummaryCompleted => 'completed';

  @override
  String get dailySummaryPointsEarnedLabel => 'pts earned';

  @override
  String get dailySummaryAmongFriends => 'among friends';

  @override
  String get weeklyReactionFriendsReactions => 'Your friends\' reactions';

  @override
  String get weeklySummaryLastWeek => 'Last week';

  @override
  String get techHealthTitle => 'Tech Health';

  @override
  String get techHealthUnavailableTitle => 'Not added on this platform yet';

  @override
  String get techHealthUnavailableBody =>
      'Tech health needs to watch app usage and block apps in real time, which today is only wired up for Android. It\'s technically possible to add this for other platforms later, but that hasn\'t been built yet — for now this feature only works on Android.';

  @override
  String get techHealthPointDialogTitle => 'Tech health point';

  @override
  String get techHealthPointDialogBody =>
      'Turn on at least 3 restrictions and don\'t exceed any limit during the day. If you manage it, you\'ll earn 10 extra points added to tomorrow\'s points.';

  @override
  String get techHealthPointBannerLabel =>
      'Point available — tap to learn more';

  @override
  String get techHealthPointLostLabel => 'Point lost today — tap to learn more';

  @override
  String get techHealthPointLostBody =>
      'You went over the limit on a restriction today, so you won\'t earn the extra point. Try again tomorrow.';

  @override
  String get techHealthRestrictionsTitle => 'Restrictions';

  @override
  String techHealthRestrictionsCount(int n) {
    return '$n total';
  }

  @override
  String get permissionsTitle => 'Set up permissions';

  @override
  String get permissionsIntro =>
      'To work properly, DayPilot needs two permissions. Don\'t worry: they\'re only used for what\'s explained here.';

  @override
  String get permissionsWarning =>
      'These are important permissions, but there\'s nothing to worry about. The app only uses them to measure your app usage and block apps once you exceed the limit.';

  @override
  String get permissionsUsageAccessTitle => 'Usage access';

  @override
  String get permissionsGranted => 'Granted';

  @override
  String get permissionsUsageAccessBody =>
      'Measures how many minutes a day you use each app. Without this, time limits can\'t be enforced.';

  @override
  String get permissionsAccessibilityTitle => 'Accessibility service';

  @override
  String get permissionsAccessibilityBody =>
      'Detects which app is in the foreground and closes it if you\'ve exceeded the limit. It doesn\'t read text or interact with any app.';

  @override
  String get permissionsPathSettings => 'Settings → ';

  @override
  String get permissionsPathAccessibility => 'Accessibility → ';

  @override
  String get permissionsPathInstalledServices => 'Installed services → ';

  @override
  String get permissionsOpenAccessibility => 'Open accessibility';

  @override
  String get loginTagline => 'Fly toward your goals';

  @override
  String get loginSignInTab => 'Sign in';

  @override
  String get loginSignUpTab => 'Create account';

  @override
  String get loginForgotPassword => 'Forgot your password?';

  @override
  String get loginSubmit => 'Log in';

  @override
  String get loginNameLabel => 'Name';

  @override
  String get loginUsernameLabel => 'Username';

  @override
  String get loginTimezoneLabel => 'Region / timezone';

  @override
  String get loginRegisterSubmit => 'Register';

  @override
  String get authErrorInvalidCredentials => 'Incorrect email or password.';

  @override
  String get authErrorEmailNotConfirmed =>
      'Please confirm your email before signing in.';

  @override
  String get authErrorAlreadyRegistered =>
      'An account with this email already exists.';

  @override
  String get authErrorWeakPassword => 'Password must be at least 6 characters.';

  @override
  String get authErrorInvalidEmail => 'Please enter a valid email address.';

  @override
  String get authErrorUnknown => 'Something went wrong. Please try again.';

  @override
  String get authErrorFillAllFields => 'Please fill in all fields.';

  @override
  String get authRegisterCheckEmail =>
      'Account created — check your email to confirm before logging in.';

  @override
  String get authErrorRateLimited =>
      'Too many attempts — please wait a few minutes before trying again.';

  @override
  String get forgotPasswordTitle => 'Recover password';

  @override
  String get forgotPasswordSentBody =>
      'Check your email, we\'ve sent you a link to reset your password.';

  @override
  String get forgotPasswordBody =>
      'Enter your email and we\'ll send you a link to reset your password';

  @override
  String get forgotPasswordSendButton => 'Send link';

  @override
  String get forgotPasswordBackToLogin => 'Back to sign in';

  @override
  String get rivalryTitle => 'Rivalry';

  @override
  String get rivalryPointsThisMonth => 'Points this month';

  @override
  String get rivalryFullRanking => 'FULL RANKING';

  @override
  String get rivalryEmpty => 'Add friends to see the ranking';

  @override
  String get progressTitle => 'Progress';

  @override
  String get habitsOtherHabits => 'Other habits';

  @override
  String get habitsTimersTitle => 'Timers';

  @override
  String get habitsTimersSubtitle => 'Pomodoro, workouts and more';

  @override
  String get habitsTechHealthSubtitle => 'Limits per app / group + alerts';

  @override
  String get remindersTitle => 'Reminders';

  @override
  String get habitsRemindersSubtitle => 'Alerts, timers and routines';

  @override
  String get friendsRequestsTab => 'Requests';

  @override
  String friendsRequestsTabCount(int n) {
    return 'Requests ($n)';
  }

  @override
  String get friendsNoRequests => 'You have no pending requests';

  @override
  String get friendsNoFriends => 'You don\'t have any friends yet';

  @override
  String get friendsRemoveConfirmTitle => 'Remove friend?';

  @override
  String friendsRemoveConfirmMessage(String name) {
    return 'Are you sure you want to remove $name from your friends?';
  }

  @override
  String get remindersEmptyState => 'You have no reminders.\nAdd one!';

  @override
  String homeTasksTodayLabel(int completed, int total) {
    return '$completed/$total tasks today';
  }

  @override
  String homeTasksLabel(int completed, int total) {
    return '$completed/$total tasks';
  }

  @override
  String get searchFriendsTitle => 'Search friends';

  @override
  String get searchFriendsHint => 'Search by name or email';

  @override
  String timerSessionOf(int current, int total) {
    return 'Session $current of $total';
  }

  @override
  String timerPhaseMinutes(int min, String phase) {
    return '$min min $phase';
  }

  @override
  String get timerPointEarned => 'Daily point earned';

  @override
  String get timerClosedAppWarning =>
      'The timer pauses if you close or minimize the app';

  @override
  String homeStepsProgressLabel(int percent) {
    return '$percent% steps';
  }

  @override
  String get homeTimerPending => 'Timer pending';

  @override
  String get trayOpen => 'Open';

  @override
  String get trayExit => 'Exit';
}
