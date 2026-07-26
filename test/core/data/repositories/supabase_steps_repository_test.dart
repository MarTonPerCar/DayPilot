import 'package:daypilot/core/data/repositories/supabase_steps_repository.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late SupabaseStepsRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    repo = SupabaseStepsRepository(http.client);
  });

  tearDown(() => http.dispose());

  test('getSteps with no signed-in user returns defaults and makes no request', () async {
    final steps = await repo.getSteps();

    expect(steps.steps, 0);
    expect(steps.goal, 10000);
    expect(steps.pointsEarnedToday, 0);
    expect(http.requests, isEmpty);
  });

  test('getSteps maps the habits_daily row and applies the milestone points table', () async {
    await http.signIn('user-1');
    http.enqueue(jsonRow({'pending_steps_goal': null, 'pending_steps_goal_date': null}));
    http.enqueue(jsonRows([
      {'steps': 4200, 'steps_goal': 8000, 'steps_milestone_level': 2},
    ]));

    final steps = await repo.getSteps();

    expect(steps.steps, 4200);
    expect(steps.goal, 8000);
    expect(steps.pointsEarnedToday, 30);
    expect(steps.pendingGoal, isNull);

    expect(http.requests, hasLength(2));
    expect(http.requests[0].url.path, '/rest/v1/users');
    expect(http.requests[0].url.queryParameters['id'], 'eq.user-1');
    expect(http.requests[1].url.path, '/rest/v1/habits_daily');
    expect(http.requests[1].url.queryParameters['user_id'], 'eq.user-1');
  });

  test('getSteps with no habits_daily row yet falls back to the 10000 default goal', () async {
    await http.signIn('user-1');
    http.enqueue(jsonRow({'pending_steps_goal': null, 'pending_steps_goal_date': null}));
    http.enqueue(emptyRows());

    final steps = await repo.getSteps();

    expect(steps.steps, 0);
    expect(steps.goal, 10000);
    expect(steps.pointsEarnedToday, 0);
  });

  test('getSteps applies a due pending goal change and clears it via upsert', () async {
    await http.signIn('user-1');
    http.enqueue(jsonRow({'pending_steps_goal': 12000, 'pending_steps_goal_date': '2020-01-01'}));
    http.enqueue(jsonRows([
      {'steps': 100, 'steps_goal': 8000, 'steps_milestone_level': 0},
    ]));
    http.enqueue(noContent());

    final steps = await repo.getSteps();

    expect(steps.goal, 12000);
    expect(steps.pendingGoal, isNull);

    expect(http.requests, hasLength(3));
    expect(http.requests[2].url.path, '/rest/v1/habits_daily');
    expect(http.requests[2].method, 'POST');
    expect(http.requests[2].headers['Prefer'], contains('resolution=merge-duplicates'));
  });

  test('getSteps propagates a PostgrestException from the server', () async {
    await http.signIn('user-1');
    http.enqueue(postgrestError(message: 'relation "users" does not exist'));

    expect(() => repo.getSteps(), throwsA(isA<PostgrestException>()));
  });

  test('setGoal with no signed-in user makes no request', () async {
    await repo.setGoal(9000);

    expect(http.requests, isEmpty);
  });

  test('setGoal writes the pending goal for tomorrow', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.setGoal(9000);

    expect(http.requests, hasLength(1));
    expect(http.requests[0].method, 'PATCH');
    expect(http.requests[0].url.path, '/rest/v1/users');
    expect(http.requests[0].url.queryParameters['id'], 'eq.user-1');
    expect(http.requests[0].body, contains('"pending_steps_goal":9000'));
  });
}
