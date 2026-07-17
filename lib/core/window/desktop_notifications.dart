import 'dart:async';

import 'package:local_notifier/local_notifier.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../data/models/app_notification_item.dart';
import '../data/notification_l10n.dart';
import '../data/notification_writer.dart';
import '../prefs/app_prefs.dart';
import '../utils/iso_date.dart';
import 'desktop_window.dart' show isDesktopPlatform;

// Mirrors Android's DailyNotificationScheduler/DailyNotificationsReceiver
// (AlarmManager + BroadcastReceiver), but desktop has no equivalent
// wake-from-fully-closed mechanism. These only fire while the app is
// actually running, via a periodic check against the wall clock instead of
// a scheduled OS alarm — acceptable given DayPilot stays tray-resident
// (launch_at_startup) most of the day.
const _taskReminderHour = 9;
const _streakDangerHour = 22;
const _checkInterval = Duration(minutes: 1);

Timer? _dailyNotificationsTimer;
bool _localNotifierReady = false;

/// Starts the periodic 09:00/22:00 check. Also runs one check immediately,
/// so logging in after either time has already passed today still fires
/// that day's notification instead of waiting for tomorrow.
void startDesktopDailyNotifications() {
  if (!isDesktopPlatform) return;
  unawaited(_checkDailyNotifications());
  _dailyNotificationsTimer ??= Timer.periodic(_checkInterval, (_) => _checkDailyNotifications());
}

void stopDesktopDailyNotifications() {
  _dailyNotificationsTimer?.cancel();
  _dailyNotificationsTimer = null;
}

Future<void> _checkDailyNotifications() async {
  final client = Supabase.instance.client;
  final uid = client.auth.currentUser?.id;
  if (uid == null) return;

  final prefs = await AppPrefs.load();
  if (!prefs.notificationsEnabled) return;

  final now = DateTime.now();
  final today = isoDate(now);

  if (prefs.taskRemindersEnabled &&
      now.hour >= _taskReminderHour &&
      prefs.taskReminderFiredDate != today) {
    await _fireTaskReminder(client, uid, prefs, today);
  }

  if (prefs.streakAlertsEnabled &&
      now.hour >= _streakDangerHour &&
      prefs.streakAlertFiredDate != today &&
      prefs.lastOpenDate != today) {
    await _fireStreakDanger(client, uid, prefs, today);
  }
}

Future<void> _fireTaskReminder(SupabaseClient client, String uid, AppPrefs prefs, String today) async {
  final l10n = currentL10n();

  int? pendingCount;
  try {
    final rows = await client.from('calendar_tasks').select('is_completed').eq('user_id', uid).eq('date', today);
    pendingCount = rows.where((r) => r['is_completed'] != true).length;
  } catch (_) {
    pendingCount = null;
  }

  final body = switch (pendingCount) {
    null => l10n.notifTaskReminderGeneric,
    0 => l10n.notifTaskReminderNone,
    final n => l10n.notifTaskReminderCount(n),
  };
  final title = l10n.notifTaskReminderTitle;

  await _showSystemNotification(title, body);
  await writeNotification(client, userId: uid, type: AppNotificationType.taskReminder, title: title, body: body);
  await prefs.setTaskReminderFiredDate(today);
}

Future<void> _fireStreakDanger(SupabaseClient client, String uid, AppPrefs prefs, String today) async {
  final l10n = currentL10n();
  final title = l10n.notifStreakDangerTitle;
  final body = l10n.notifStreakDangerBody;

  await _showSystemNotification(title, body);
  await writeNotification(client, userId: uid, type: AppNotificationType.streakRisk, title: title, body: body);
  await prefs.setStreakAlertFiredDate(today);
}

Future<void> _showSystemNotification(String title, String body) async {
  if (!_localNotifierReady) {
    await localNotifier.setup(appName: 'DayPilot');
    _localNotifierReady = true;
  }
  await LocalNotification(title: title, body: body).show();
}
