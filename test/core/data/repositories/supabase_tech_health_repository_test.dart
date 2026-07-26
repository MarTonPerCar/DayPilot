import 'package:daypilot/core/data/repositories/supabase_tech_health_repository.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseTechHealthRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseTechHealthRepository(http.client);
  });

  tearDown(() => http.dispose());

  group('getRestrictions', () {
    test('with no signed-in user returns empty and makes no request', () async {
      final restrictions = await repo.getRestrictions();

      expect(restrictions, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('maps rows and converts the hour limit to minutes', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'app_package': 'com.instagram', 'app_name': 'Instagram', 'limit_hours': 1.5, 'is_active': true},
      ]));

      final restrictions = await repo.getRestrictions();

      expect(restrictions, hasLength(1));
      expect(restrictions.single.limitMinutes, 90);
      expect(http.requests[0].url.queryParameters['user_id'], 'eq.user-1');
      expect(http.requests[0].url.queryParameters['pending_delete'], 'eq.false');
    });
  });

  test('saveRestriction with no signed-in user makes no request', () async {
    await repo.saveRestriction(appPackage: 'com.instagram', appName: 'Instagram', limitMinutes: 90);

    expect(http.requests, isEmpty);
  });

  test('saveRestriction upserts the limit and clears a pending delete', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());
    http.enqueue(noContent());

    await repo.saveRestriction(appPackage: 'com.instagram', appName: 'Instagram', limitMinutes: 90);

    expect(http.requests, hasLength(2));
    expect(http.requests[0].headers['Prefer'], contains('resolution=merge-duplicates'));
    expect(http.requests[0].body, contains('"limit_hours":1.5'));
    expect(http.requests[1].body, contains('"pending_delete":false'));
  });

  test('toggleRestriction with no signed-in user makes no request', () async {
    await repo.toggleRestriction('com.instagram', false);

    expect(http.requests, isEmpty);
  });

  test('toggleRestriction updates is_active for the given app', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.toggleRestriction('com.instagram', false);

    expect(http.requests[0].body, contains('"is_active":false'));
    expect(http.requests[0].url.queryParameters['app_package'], 'eq.com.instagram');
  });

  test('deleteRestriction with no signed-in user makes no request', () async {
    await repo.deleteRestriction('com.instagram');

    expect(http.requests, isEmpty);
  });

  test('deleteRestriction marks the row as pending delete instead of removing it', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.deleteRestriction('com.instagram');

    expect(http.requests[0].method, 'PATCH');
    expect(http.requests[0].body, contains('"pending_delete":true'));
  });

  group('getPointEarnedToday', () {
    test('with no signed-in user returns false and makes no request', () async {
      final earned = await repo.getPointEarnedToday();

      expect(earned, isFalse);
      expect(http.requests, isEmpty);
    });

    test('with fewer than 3 active restrictions returns false', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'is_violated_today': false},
        {'is_violated_today': false},
      ]));

      final earned = await repo.getPointEarnedToday();

      expect(earned, isFalse);
    });

    test('with 3+ active restrictions and none violated returns true', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'is_violated_today': false},
        {'is_violated_today': false},
        {'is_violated_today': false},
      ]));

      final earned = await repo.getPointEarnedToday();

      expect(earned, isTrue);
    });

    test('with any restriction violated today returns false', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'is_violated_today': false},
        {'is_violated_today': true},
        {'is_violated_today': false},
      ]));

      final earned = await repo.getPointEarnedToday();

      expect(earned, isFalse);
    });
  });
}
