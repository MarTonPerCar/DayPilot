import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/models/app_notification_item.dart';
import '../../core/data/repositories/providers.dart';
import '../friends/friends_notifier.dart';
import '../profile/weekly_summary_notifier.dart';

class NotificationsNotifier extends Notifier<List<AppNotificationItem>> {
  RealtimeChannel? _channel;

  @override
  List<AppNotificationItem> build() {
    Future.microtask(refresh);
    ref.onDispose(() => _channel?.unsubscribe());
    return const [];
  }

  Future<void> refresh() async {
    state = await ref.read(notificationsRepositoryProvider).getNotifications();
    _subscribeToRealtimeOnce();
  }

  // These notifications never produce an OS banner, so the in-app list must update live.
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
    if (id == null || state.any((n) => n.id == id)) return; // already have it from a refresh()
    final type = AppNotificationTypeDb.fromDb(row['type'] as String);
    state = [
      AppNotificationItem(
        id: id,
        type: type,
        title: row['title'] as String,
        body: row['body'] as String,
        isRead: row['is_read'] as bool? ?? false,
        createdAt: DateTime.parse(row['created_at'] as String),
      ),
      ...state,
    ];

    // Friend requests/accepts and reactions don't have their own realtime
    // subscription wired to their respective screens, so ride in on this
    // one — otherwise Friends/Profile would stay stale until the next
    // login, manual refresh, or (for the weekly summary) up to 5 minutes.
    if (type == AppNotificationType.friendRequest || type == AppNotificationType.friendAccepted) {
      ref.read(friendsNotifierProvider.notifier).refresh();
    } else if (type == AppNotificationType.reaction) {
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
