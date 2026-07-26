import '../../core/data/models/app_reminder.dart';

const earlyWarningWindow = Duration(minutes: 10);

/// Whether the 10-minutes-before warning should fire right now for [reminder].
bool isEarlyDue(AppReminder reminder, DateTime now) {
  return reminder.notifyBefore &&
      now.isAfter(reminder.dateTime.subtract(earlyWarningWindow)) &&
      now.isBefore(reminder.dateTime);
}

/// Whether the main reminder notification is due to fire right now.
bool isMainDue(AppReminder reminder, DateTime now) => !now.isBefore(reminder.dateTime);

/// The reminder's next `dateTime` after firing, or null if it should be deleted
/// (a one-off "once" reminder, or an unrecognized frequency).
DateTime? nextOccurrence(AppReminder reminder) {
  switch (reminder.frequency) {
    case 'daily':
      return reminder.dateTime.add(const Duration(days: 1));
    case 'weekly':
      return reminder.dateTime.add(const Duration(days: 7));
    default:
      return null;
  }
}
