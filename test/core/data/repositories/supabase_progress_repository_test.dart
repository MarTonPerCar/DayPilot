import 'package:daypilot/core/data/repositories/supabase_progress_repository.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseProgressRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseProgressRepository(http.client);
  });

  tearDown(() => http.dispose());

  group('getProgress', () {
    test('with no signed-in user returns all zeros and makes no request', () async {
      final progress = await repo.getProgress();

      expect(progress.pointsToday, 0);
      expect(progress.pointsHistory, isEmpty);
      expect(progress.dayLabels, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('with no rows yet returns zeros for today but still includes today in the history', () async {
      await http.signIn('user-1');
      http.enqueue(emptyRows());
      http.enqueue(emptyRows());

      final progress = await repo.getProgress();

      expect(progress.pointsToday, 0);
      expect(progress.pointsFromHabits, 0);
      expect(progress.pointsHistory, [0.0]);
      expect(progress.dayLabels, [DateTime.now().day]);
    });

    test('combines today\'s row with history rows, oldest first', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {
          'total_points': 90,
          'tasks_points': 20,
          'steps_points': 10,
          'wellness_points': 15,
          'tech_health_points': 5,
          'timer_points': 40,
        },
      ]));
      http.enqueue(jsonRows([
        {'date': '2026-07-24', 'total_points': 60, 'steps': 4000, 'tasks_completed': 2},
      ]));

      final progress = await repo.getProgress();

      expect(progress.pointsToday, 90);
      expect(progress.pointsFromTasks, 20);
      expect(progress.pointsFromSteps, 10);
      expect(progress.pointsFromHabits, 20);
      expect(progress.pointsFromTimer, 40);
      expect(progress.pointsHistory, [60.0, 90.0]);
      expect(progress.stepsHistory, [4000.0, 0.0]);
      expect(progress.tasksHistory, [2.0, 0.0]);
      expect(progress.dayLabels, [24, DateTime.now().day]);

      expect(http.requests[1].url.queryParameters['limit'], '29');
      expect(http.requests[1].url.queryParameters['order'], 'date.desc.nullslast');
    });
  });

  group('completeTimerSession', () {
    test('with no signed-in user returns false and makes no request', () async {
      final earned = await repo.completeTimerSession();

      expect(earned, isFalse);
      expect(http.requests, isEmpty);
    });

    test('when the point was already earned today returns false without writing', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {'timer_point_earned': true},
      ]));

      final earned = await repo.completeTimerSession();

      expect(earned, isFalse);
      expect(http.requests, hasLength(1));
    });

    test('logs the timer points and marks the day as earned', () async {
      await http.signIn('user-1');
      http.enqueue(emptyRows());
      http.enqueue(noContent());
      http.enqueue(noContent());

      final earned = await repo.completeTimerSession();

      expect(earned, isTrue);
      expect(http.requests, hasLength(3));
      expect(http.requests[1].url.path, '/rest/v1/points_log');
      expect(http.requests[1].body, contains('"points":10'));
      expect(http.requests[1].body, contains('"source":"TIMER"'));
      expect(http.requests[2].url.path, '/rest/v1/habits_daily');
      expect(http.requests[2].body, contains('"timer_point_earned":true'));
    });
  });
}
