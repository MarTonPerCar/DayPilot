import 'dart:async';

import 'package:local_notifier/local_notifier.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../data/models/app_notification_item.dart';
import '../logging/app_logger.dart';
import '../prefs/app_prefs.dart';
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

  try {
    final startOfDay = DateTime(DateTime.now().year, DateTime.now().month, DateTime.now().day);

    final rows = await client
        .from('notifications')
        .select()
        .eq('user_id', uid)
        .inFilter('type', _dailyNotificationTypes.toList())
        .gte('created_at', startOfDay.toUtc().toIso8601String());

    for (final row in rows) {
      final item = AppNotificationItem.fromRow(row);
      await _maybeShowNative(type: row['type'] as String, title: item.title, body: item.body);
    }
  } catch (e, st) {
    AppLogger.logError('checkForUnseenNotificationsToday', e, st);
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
          unawaited(_maybeShowNative(type: type, title: item.title, body: item.body));
        },
      )
      .subscribe();
}

Future<void> _maybeShowNative({
  required String type,
  required String title,
  required String body,
}) async {
  final prefs = await AppPrefs.load();
  if (!prefs.notificationsEnabled) return;
  if (type == 'TASK_REMINDER' && !prefs.taskRemindersEnabled) return;
  if (type == 'STREAK_RISK' && !prefs.streakAlertsEnabled) return;

  await showDesktopNotification(title, body);
}

/// Shows a native OS notification via `local_notifier`. Desktop only (Windows/Linux/macOS) —
/// callers should guard with [isDesktopPlatform] first.
Future<void> showDesktopNotification(String title, String body) async {
  if (!_localNotifierReady) {
    await localNotifier.setup(appName: 'DayPilot');
    _localNotifierReady = true;
  }
  await LocalNotification(title: title, body: body).show();
}
