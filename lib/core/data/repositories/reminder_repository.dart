import '../models/app_reminder.dart';

abstract class ReminderRepository {
  Future<List<AppReminder>> getReminders();
  Future<AppReminder> addReminder(AppReminder reminder);
  Future<void> deleteReminder(String id);
  Future<void> toggleReminder(String id, bool enabled);
  Future<void> updateDateTime(String id, DateTime dateTime);
}
