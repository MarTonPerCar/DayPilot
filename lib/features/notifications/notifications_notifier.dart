import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/data/models/app_notification_item.dart';
import '../../core/data/repositories/providers.dart';

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

  // "Small" notifications (friend requests, reactions, level-up, etc.) never
  // produce an OS banner — the in-app list/badge is the only delivery
  // mechanism, so it must update live. One channel per notifier lifetime;
  // refresh() can be called repeatedly (e.g. after awarding points elsewhere)
  // without re-subscribing each time.
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
    state = [
      AppNotificationItem(
        id: id,
        type: AppNotificationTypeDb.fromDb(row['type'] as String),
        title: row['title'] as String,
        body: row['body'] as String,
        isRead: row['is_read'] as bool? ?? false,
        createdAt: DateTime.parse(row['created_at'] as String),
      ),
      ...state,
    ];
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
