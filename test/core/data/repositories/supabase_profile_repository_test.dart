import 'dart:typed_data';

import 'package:daypilot/core/data/repositories/supabase_profile_repository.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseProfileRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseProfileRepository(http.client);
  });

  tearDown(() => http.dispose());

  group('getProfileStats', () {
    test('with no signed-in user returns a blank level-1 profile and makes no request', () async {
      final stats = await repo.getProfileStats();

      expect(stats.level, 1);
      expect(stats.totalPoints, 0);
      expect(http.requests, isEmpty);
    });

    test('computes xp progress from the level threshold', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRow({
        'name': 'Ana',
        'username': 'ana',
        'email': 'ana@daypilot.test',
        'created_at': '2024-01-01T00:00:00Z',
        'region': 'ES',
        'photo_url': null,
        'level': 3,
        'total_points_historical': 140,
        'points_to_next_level': 200,
      }));
      http.enqueue(jsonRows([
        {'current_streak': 5, 'longest_streak': 12},
      ]));

      final stats = await repo.getProfileStats();

      // _levelThreshold(2) = 5*2*5 = 50
      expect(stats.currentXp, 90);
      expect(stats.xpToNextLevel, 150);
      expect(stats.totalPoints, 140);
      expect(stats.streak, 5);
      expect(stats.bestStreak, 12);
      expect(http.requests[0].url.queryParameters['id'], 'eq.user-1');
    });

    test('with no streak row yet defaults streaks to zero', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRow({
        'name': 'Ana',
        'username': 'ana',
        'email': 'ana@daypilot.test',
        'created_at': '2024-01-01T00:00:00Z',
        'region': null,
        'photo_url': null,
        'level': 1,
        'total_points_historical': 0,
        'points_to_next_level': 50,
      }));
      http.enqueue(emptyRows());

      final stats = await repo.getProfileStats();

      expect(stats.streak, 0);
      expect(stats.bestStreak, 0);
      expect(stats.region, '');
    });
  });

  group('getWeeklySummary', () {
    test('with no signed-in user returns an empty summary and makes no request', () async {
      final summary = await repo.getWeeklySummary();

      expect(summary.totalPoints, 0);
      expect(summary.reactions, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('with no summary row yet returns an empty summary after one request', () async {
      await http.signIn('user-1');
      http.enqueue(emptyRows());

      final summary = await repo.getWeeklySummary();

      expect(summary.totalPoints, 0);
      expect(http.requests, hasLength(1));
    });

    test('maps the latest summary and its reactions with sender profiles', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'id': 'summary-1', 'total_points': 300, 'total_steps': 9000, 'total_tasks_completed': 8, 'best_streak': 6},
      ]));
      http.enqueue(jsonRows([
        {'from_user_id': 'friend-2', 'type': 'clap'},
      ]));
      http.enqueue(jsonRows([
        {'id': 'friend-2', 'name': 'Bob', 'photo_url': 'bob.png'},
      ]));

      final summary = await repo.getWeeklySummary();

      expect(summary.totalPoints, 300);
      expect(summary.reactions, hasLength(1));
      expect(summary.reactions.single.fromName, 'Bob');
      expect(summary.reactions.single.emoji, '👏');

      expect(http.requests[0].url.queryParameters['limit'], '1');
    });
  });

  test('updateProfile with no signed-in user makes no request', () async {
    await repo.updateProfile(name: 'Ana', username: 'ana', region: 'ES');

    expect(http.requests, isEmpty);
  });

  test('updateProfile lower-cases the username for the unique index', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.updateProfile(name: 'Ana García', username: 'AnaG', region: 'ES');

    expect(http.requests[0].method, 'PATCH');
    expect(http.requests[0].url.path, '/rest/v1/users');
    expect(http.requests[0].body, contains('"username_lower":"anag"'));
  });

  test('changePassword forwards the new password to auth', () async {
    await http.signIn('user-1');
    http.enqueue(jsonRow({'id': 'user-1', 'aud': 'authenticated', 'created_at': '2024-01-01T00:00:00Z'}));

    await repo.changePassword('new-password-123');

    expect(http.requests, hasLength(1));
    expect(http.requests[0].url.path, '/auth/v1/user');
    expect(http.requests[0].body, contains('"password":"new-password-123"'));
  });

  test('uploadAvatar with no signed-in user throws', () async {
    expect(
      () => repo.uploadAvatar(bytes: Uint8List(0), fileExtension: 'png'),
      throwsA(isA<StateError>()),
    );
  });

  test('uploadAvatar stores the file then persists the public URL', () async {
    await http.signIn('user-1');
    http.enqueue(jsonRow({'Key': 'avatars/user-1/1.png'}));
    http.enqueue(noContent());

    final url = await repo.uploadAvatar(bytes: Uint8List.fromList([1, 2, 3]), fileExtension: 'png');

    expect(url, contains('/storage/v1/object/public/avatars/user-1/'));
    expect(http.requests[0].method, 'POST');
    expect(http.requests[0].url.path, contains('/storage/v1/object/avatars/user-1/'));
    expect(http.requests[1].url.path, '/rest/v1/users');
    expect(http.requests[1].body, contains('"photo_url"'));
  });
}
