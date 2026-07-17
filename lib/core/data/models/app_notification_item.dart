import '../notification_l10n.dart';

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

  factory AppNotificationItem.fromRow(Map<String, dynamic> row) {
    final type = AppNotificationTypeDb.fromDb(row['type'] as String);
    final rawTitle = row['title'] as String;
    final rawBody = row['body'] as String;
    final (title, body) = _localizedText(type, rawTitle, rawBody);

    return AppNotificationItem(
      id: row['id'] as String,
      type: type,
      title: title,
      body: body,
      isRead: row['is_read'] as bool? ?? false,
      createdAt: DateTime.parse(row['created_at'] as String),
    );
  }

  final String id;
  final AppNotificationType type;
  final String title;
  final String body;
  final bool isRead;
  final DateTime createdAt;
}

(String title, String body) _localizedText(AppNotificationType type, String rawTitle, String rawBody) {
  final l10n = currentL10n();
  switch (type) {
    case AppNotificationType.taskReminder:
      final body = switch (rawBody) {
        'TASK_REMINDER_NONE' => l10n.notifTaskReminderNone,
        final s when s.startsWith('TASK_REMINDER_COUNT:') =>
          l10n.notifTaskReminderCount(int.parse(s.split(':')[1])),
        _ => l10n.notifTaskReminderGeneric,
      };
      return (l10n.notifTaskReminderTitle, body);
    case AppNotificationType.streakRisk:
      return (l10n.notifStreakDangerTitle, l10n.notifStreakDangerBody);
    default:
      return (rawTitle, rawBody);
  }
}
