enum AppNotificationType {
  friendRequest,
  friendAccepted,
  reaction,
  levelUp,
  streakRisk,
  stepsGoal,
  taskCompleted,
  timerDone,
  taskReminder,
  dailySummary,
}

extension AppNotificationTypeDb on AppNotificationType {
  String get dbValue => switch (this) {
        AppNotificationType.friendRequest => 'FRIEND_REQUEST',
        AppNotificationType.friendAccepted => 'FRIEND_ACCEPTED',
        AppNotificationType.reaction => 'REACTION',
        AppNotificationType.levelUp => 'LEVEL_UP',
        AppNotificationType.streakRisk => 'STREAK_RISK',
        AppNotificationType.stepsGoal => 'STEPS_GOAL',
        AppNotificationType.taskCompleted => 'TASK_COMPLETED',
        AppNotificationType.timerDone => 'TIMER_DONE',
        AppNotificationType.taskReminder => 'TASK_REMINDER',
        AppNotificationType.dailySummary => 'DAILY_SUMMARY',
      };

  static AppNotificationType fromDb(String value) => switch (value) {
        'FRIEND_REQUEST' => AppNotificationType.friendRequest,
        'FRIEND_ACCEPTED' => AppNotificationType.friendAccepted,
        'REACTION' => AppNotificationType.reaction,
        'LEVEL_UP' => AppNotificationType.levelUp,
        'STREAK_RISK' => AppNotificationType.streakRisk,
        'STEPS_GOAL' => AppNotificationType.stepsGoal,
        'TASK_COMPLETED' => AppNotificationType.taskCompleted,
        'TIMER_DONE' => AppNotificationType.timerDone,
        'TASK_REMINDER' => AppNotificationType.taskReminder,
        _ => AppNotificationType.dailySummary,
      };
}

class AppNotificationItem {
  const AppNotificationItem({
    required this.id,
    required this.type,
    required this.title,
    required this.body,
    required this.isRead,
    required this.createdAt,
  });

  final String id;
  final AppNotificationType type;
  final String title;
  final String body;
  final bool isRead;
  final DateTime createdAt;
}
