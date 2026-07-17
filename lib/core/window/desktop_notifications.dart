import 'dart:async';

import 'package:local_notifier/local_notifier.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../data/models/app_notification_item.dart';
import '../prefs/app_prefs.dart';
import '../utils/iso_date.dart';
import 'desktop_window.dart' show isDesktopPlatform;

const _dailyNotificationTypes = {'TASK_REMINDER', 'STREAK_RISK'};

RealtimeChannel? _channel;
bool _localNotifierReady = false;

void startDesktopDailyNotifications() {
  if (!isDesktopPlatform) return;
  unawaited(_checkForUnseenNotificationsToday());
  _subscribeToNewNotifications();
}

void stopDesktopDailyNotifications() {
  _channel?.unsubscribe();
  _channel = null;
}

Future<void> _checkForUnseenNotificationsToday() async {
  final client = Supabase.instance.client;
  final uid = client.auth.currentUser?.id;
  if (uid == null) return;

  final today = isoDate(DateTime.now());
  final startOfDay = DateTime(DateTime.now().year, DateTime.now().month, DateTime.now().day);

  final rows = await client
      .from('notifications')
      .select()
      .eq('user_id', uid)
      .inFilter('type', _dailyNotificationTypes.toList())
      .gte('created_at', startOfDay.toUtc().toIso8601String());

  for (final row in rows) {
    final item = AppNotificationItem.fromRow(row);
    await _maybeShowNative(
      type: row['type'] as String,
      title: item.title,
      body: item.body,
      today: today,
    );
  }
}

void _subscribeToNewNotifications() {
  if (_channel != null) return;
  final client = Supabase.instance.client;
  final uid = client.auth.currentUser?.id;
  if (uid == null) return;

  _channel = client
      .channel('desktop-notifications-$uid')
      .onPostgresChanges(
        event: PostgresChangeEvent.insert,
        schema: 'public',
        table: 'notifications',
        filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'user_id', value: uid),
        callback: (payload) {
          final row = payload.newRecord;
          final type = row['type'] as String?;
          if (type == null || !_dailyNotificationTypes.contains(type)) return;
          final item = AppNotificationItem.fromRow(row);
          unawaited(_maybeShowNative(
            type: type,
            title: item.title,
            body: item.body,
            today: isoDate(DateTime.now()),
          ));
        },
      )
      .subscribe();
}

Future<void> _maybeShowNative({
  required String type,
  required String title,
  required String body,
  required String today,
}) async {
  final prefs = await AppPrefs.load();
  if (!prefs.notificationsEnabled) return;

  if (type == 'TASK_REMINDER') {
    if (!prefs.taskRemindersEnabled) return;
    if (prefs.taskReminderFiredDate == today) return;
    await prefs.setTaskReminderFiredDate(today);
  } else if (type == 'STREAK_RISK') {
    if (!prefs.streakAlertsEnabled) return;
    if (prefs.streakAlertFiredDate == today) return;
    await prefs.setStreakAlertFiredDate(today);
  }

  await _showSystemNotification(title, body);
}

Future<void> _showSystemNotification(String title, String body) async {
  if (!_localNotifierReady) {
    await localNotifier.setup(appName: 'DayPilot');
    _localNotifierReady = true;
  }
  await LocalNotification(title: title, body: body).show();
}
