import 'package:daypilot/core/data/models/app_notification_item.dart';
import 'package:daypilot/core/data/repositories/supabase_notifications_repository.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseNotificationsRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseNotificationsRepository(http.client);
  });

  tearDown(() => http.dispose());

  group('getNotifications', () {
    test('with no signed-in user returns empty and makes no request', () async {
      final notifications = await repo.getNotifications();

      expect(notifications, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('maps rows newest-first', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {
          'id': 'n1',
          'type': 'REACTION',
          'title': 'Ana reacted',
          'body': 'to your week',
          'is_read': false,
          'created_at': '2026-07-25T10:00:00Z',
        },
      ]));

      final notifications = await repo.getNotifications();

      expect(notifications, hasLength(1));
      expect(notifications.single.type, AppNotificationType.reaction);
      expect(notifications.single.title, 'Ana reacted');
      expect(notifications.single.isRead, isFalse);

      expect(http.requests[0].url.queryParameters['user_id'], 'eq.user-1');
      expect(http.requests[0].url.queryParameters['order'], 'created_at.desc.nullslast');
    });
  });

  test('markAsRead updates the given notification row', () async {
    http.enqueue(noContent());

    await repo.markAsRead('n1');

    expect(http.requests[0].method, 'PATCH');
    expect(http.requests[0].url.path, '/rest/v1/notifications');
    expect(http.requests[0].url.queryParameters['id'], 'eq.n1');
    expect(http.requests[0].body, contains('"is_read":true'));
  });

  test('markAllAsRead with no signed-in user makes no request', () async {
    await repo.markAllAsRead();

    expect(http.requests, isEmpty);
  });

  test('markAllAsRead updates only the unread rows for the signed-in user', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.markAllAsRead();

    expect(http.requests[0].url.queryParameters['user_id'], 'eq.user-1');
    expect(http.requests[0].url.queryParameters['is_read'], 'eq.false');
  });
}
