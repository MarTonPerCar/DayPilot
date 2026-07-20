import 'dart:async';

import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/models/app_notification_item.dart';
import '../../core/data/repositories/providers.dart';
import '../../core/logging/app_logger.dart';
import '../friends/friends_notifier.dart';
import '../profile/weekly_summary_notifier.dart';

class NotificationsNotifier extends Notifier<List<AppNotificationItem>> {
  // See FriendsNotifier for why this fallback exists — Realtime's first subscribe() after app
  // start isn't instant, and missed Postgres changes are never replayed.
  static const _refreshInterval = Duration(minutes: 5);
  RealtimeChannel? _channel;

  @override
  List<AppNotificationItem> build() {
    Future.microtask(refresh);
    final timer = Timer.periodic(_refreshInterval, (_) => refresh());
    ref.onDispose(() {
      timer.cancel();
      _channel?.unsubscribe();
    });
    return const [];
  }

  Future<void> refresh() async {
    try {
      state = await ref.read(notificationsRepositoryProvider).getNotifications();
      _subscribeToRealtimeOnce();
    } catch (e, st) {
      AppLogger.logError('NotificationsNotifier.refresh', e, st);
    }
  }

  void _subscribeToRealtimeOnce() {
    if (_channel != null) return;
    final uid = ref.read(supabaseClientProvider).auth.currentUser?.id;
    if (uid == null) return;

    _channel = ref
        .read(supabaseClientProvider)
        .channel('notifications-$uid')
        .onPostgresChanges(
          event: PostgresChangeEvent.insert,
          schema: 'public',
          table: 'notifications',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'user_id', value: uid),
          callback: (payload) => _handleInsert(payload.newRecord),
        )
        .subscribe();
  }

  void _handleInsert(Map<String, dynamic> row) {
    final id = row['id'] as String?;
    if (id == null || state.any((n) => n.id == id)) return;
    final item = AppNotificationItem.fromRow(row);
    state = [item, ...state];

    if (item.type == AppNotificationType.friendRequest || item.type == AppNotificationType.friendAccepted) {
      ref.read(friendsNotifierProvider.notifier).refresh();
    } else if (item.type == AppNotificationType.reaction) {
      ref.read(weeklySummaryNotifierProvider.notifier).refresh();
    }
  }

  Future<void> markAsRead(String id) async {
    await ref.read(notificationsRepositoryProvider).markAsRead(id);
    state = [
      for (final n in state)
        if (n.id == id)
          AppNotificationItem(id: n.id, type: n.type, title: n.title, body: n.body, isRead: true, createdAt: n.createdAt)
        else
          n,
    ];
  }

  Future<void> markAllAsRead() async {
    await ref.read(notificationsRepositoryProvider).markAllAsRead();
    state = [
      for (final n in state)
        AppNotificationItem(id: n.id, type: n.type, title: n.title, body: n.body, isRead: true, createdAt: n.createdAt),
    ];
  }
}

final notificationsNotifierProvider =
    NotifierProvider<NotificationsNotifier, List<AppNotificationItem>>(NotificationsNotifier.new);
