import 'package:daypilot/core/cache/session_cache.dart';
import 'package:daypilot/core/data/models/app_task.dart';
import 'package:daypilot/core/data/models/task_category.dart';
import 'package:daypilot/core/data/models/task_difficulty.dart';
import 'package:daypilot/core/data/repositories/supabase_task_repository.dart';
import 'package:daypilot/core/utils/iso_date.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';

import '../../../support/fake_supabase_http.dart';

void main() {
  late FakeSupabaseHttp http;
  late ProviderContainer container;
  late SupabaseTaskRepository repo;

  setUp(() {
    http = FakeSupabaseHttp();
    container = ProviderContainer();
    final repoProvider = Provider((ref) => SupabaseTaskRepository(http.client, ref));
    repo = container.read(repoProvider);
  });

  tearDown(() {
    container.dispose();
    return http.dispose();
  });

  group('getTasks', () {
    test('returns the cached list without making a request', () async {
      final cached = [
        AppTask(
          id: 't1',
          occurrenceId: 'o1',
          title: 'Cached',
          difficulty: TaskDifficulty.easy,
          category: TaskCategory.otro,
          date: DateTime(2026, 7, 25),
          durationMinutes: 10,
        ),
      ];
      container.read(tasksCacheProvider.notifier).state = cached;

      final tasks = await repo.getTasks();

      expect(tasks, cached);
      expect(http.requests, isEmpty);
    });

    test('with no signed-in user and no cache returns empty', () async {
      final tasks = await repo.getTasks();

      expect(tasks, isEmpty);
      expect(http.requests, isEmpty);
    });

    test('fetches, maps and caches tasks for the signed-in user', () async {
      await http.signIn('user-1');
      http.enqueue(jsonRows([
        {
          'task_id': 't1',
          'occurrence_id': 'o1',
          'title': 'Read',
          'description': null,
          'difficulty': 'MEDIUM',
          'category': 'Estudio',
          'date': '2026-07-25',
          'estimated_minutes': 30,
          'reminder_enabled': true,
          'is_recurring': false,
          'is_completed': false,
        },
      ]));

      final tasks = await repo.getTasks();

      expect(tasks, hasLength(1));
      expect(tasks.single.title, 'Read');
      expect(tasks.single.difficulty, TaskDifficulty.medium);
      expect(tasks.single.category, TaskCategory.estudio);
      expect(http.requests[0].url.queryParameters['user_id'], 'eq.user-1');

      // A second call should now be served from the cache the first call populated.
      final cachedTasks = await repo.getTasks();
      expect(cachedTasks, hasLength(1));
      expect(http.requests, hasLength(1));
    });
  });

  test('addTask with no signed-in user makes no request', () async {
    await repo.addTask(NewTaskData(
      date: DateTime(2026, 7, 25),
      title: 'Read',
      category: TaskCategory.estudio,
      difficulty: TaskDifficulty.easy,
      durationMinutes: 20,
    ));

    expect(http.requests, isEmpty);
  });

  test('addTask inserts the task and its single occurrence when not recurring', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());
    http.enqueue(noContent());

    await repo.addTask(NewTaskData(
      date: DateTime(2026, 7, 25),
      title: 'Read',
      category: TaskCategory.estudio,
      difficulty: TaskDifficulty.hard,
      durationMinutes: 20,
    ));

    expect(http.requests, hasLength(2));
    expect(http.requests[0].url.path, '/rest/v1/tasks');
    expect(http.requests[0].body, contains('"title":"Read"'));
    expect(http.requests[0].body, contains('"category":"Estudio"'));
    expect(http.requests[0].body, contains('"difficulty":"HARD"'));
    expect(http.requests[1].url.path, '/rest/v1/task_days');
    expect(http.requests[1].body, contains('"date":"2026-07-25"'));
  });

  test('addTask expands a recurring task into occurrences up to 90 days out', () async {
    await http.signIn('user-1');
    for (var i = 0; i < 4; i++) {
      http.enqueue(noContent());
    }

    await repo.addTask(NewTaskData(
      date: DateTime(2026, 1, 1),
      title: 'Standup',
      category: TaskCategory.trabajo,
      difficulty: TaskDifficulty.easy,
      durationMinutes: 15,
      recurring: true,
      recurrenceDays: 45,
    ));

    // 1 tasks insert + 1 initial task_days insert + 2 recurring occurrences (day 45, day 90).
    expect(http.requests, hasLength(4));
    expect(http.requests[1].body, contains('"date":"${isoDate(DateTime(2026, 1, 1))}"'));
    expect(http.requests[2].body, contains('"date":"${isoDate(DateTime(2026, 1, 1).add(const Duration(days: 45)))}"'));
    expect(http.requests[3].body, contains('"date":"${isoDate(DateTime(2026, 1, 1).add(const Duration(days: 90)))}"'));
  });

  test('updateTask patches the editable fields scoped to the owner', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.updateTask(
      id: 't1',
      title: 'New title',
      description: '',
      category: TaskCategory.salud,
      difficulty: TaskDifficulty.medium,
      durationMinutes: 25,
    );

    expect(http.requests[0].method, 'PATCH');
    expect(http.requests[0].url.path, '/rest/v1/tasks');
    expect(http.requests[0].url.queryParameters['id'], 'eq.t1');
    expect(http.requests[0].url.queryParameters['user_id'], 'eq.user-1');
    expect(http.requests[0].body, contains('"description":null'));
  });

  group('toggleTask', () {
    test('marking as not-done only updates task_days', () async {
      await http.signIn('user-1');
      http.enqueue(noContent());

      await repo.toggleTask(occurrenceId: 'o1', isDone: false);

      expect(http.requests, hasLength(1));
      expect(http.requests[0].body, contains('"is_completed":false'));
      expect(http.requests[0].body, contains('"completed_at":null'));
    });

    test('marking as done for the first time earns points', () async {
      await http.signIn('user-1');
      http.enqueue(noContent());
      http.enqueue(jsonRows([
        {'is_earned': false},
      ]));
      http.enqueue(noContent());
      http.enqueue(noContent());

      await repo.toggleTask(occurrenceId: 'o1', isDone: true);

      expect(http.requests, hasLength(4));
      expect(http.requests[2].url.path, '/rest/v1/points_log');
      expect(http.requests[2].body, contains('"points":20'));
      expect(http.requests[3].body, contains('"is_earned":true'));
    });

    test('marking as done when already earned does not award points again', () async {
      await http.signIn('user-1');
      http.enqueue(noContent());
      http.enqueue(jsonRows([
        {'is_earned': true},
      ]));

      await repo.toggleTask(occurrenceId: 'o1', isDone: true);

      expect(http.requests, hasLength(2));
    });
  });

  test('deleteTask deletes the row scoped to the owner', () async {
    await http.signIn('user-1');
    http.enqueue(noContent());

    await repo.deleteTask('t1');

    expect(http.requests[0].method, 'DELETE');
    expect(http.requests[0].url.path, '/rest/v1/tasks');
    expect(http.requests[0].url.queryParameters['id'], 'eq.t1');
    expect(http.requests[0].url.queryParameters['user_id'], 'eq.user-1');
  });
}
