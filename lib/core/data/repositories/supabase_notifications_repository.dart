import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_notification_item.dart';
import 'notifications_repository.dart';

class SupabaseNotificationsRepository implements NotificationsRepository {
  SupabaseNotificationsRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<List<AppNotificationItem>> getNotifications() async {
    final uid = _userId;
    if (uid == null) return [];

    final rows = await _client
        .from('notifications')
        .select()
        .eq('user_id', uid)
        .order('created_at', ascending: false);

    return [
      for (final r in rows)
        AppNotificationItem(
          id: r['id'] as String,
          type: AppNotificationTypeDb.fromDb(r['type'] as String),
          title: r['title'] as String,
          body: r['body'] as String,
          isRead: r['is_read'] as bool,
          createdAt: DateTime.parse(r['created_at'] as String),
        ),
    ];
  }

  @override
  Future<void> markAsRead(String id) async {
    await _client.from('notifications').update({'is_read': true}).eq('id', id);
  }

  @override
  Future<void> markAllAsRead() async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('notifications').update({'is_read': true}).eq('user_id', uid).eq('is_read', false);
  }
}
