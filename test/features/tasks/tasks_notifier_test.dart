import 'package:daypilot/core/data/models/app_task.dart';
import 'package:daypilot/core/data/models/task_category.dart';
import 'package:daypilot/core/data/models/task_difficulty.dart';
import 'package:daypilot/core/connectivity/connectivity_service.dart';
import 'package:daypilot/core/data/repositories/providers.dart';
import 'package:daypilot/features/tasks/task_error.dart';
import 'package:daypilot/features/tasks/tasks_notifier.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import '../../support/fake_connectivity.dart';
import '../../support/mocks.dart';

void main() {
  late MockTaskRepository taskRepo;
  late FakeConnectivityService connectivity;
  late ProviderContainer container;

  final existingTask = AppTask(
    id: 't1',
    occurrenceId: 'o1',
    title: 'Existing task',
    difficulty: TaskDifficulty.easy,
    category: TaskCategory.personal,
    date: DateTime(2026, 7, 24),
    durationMinutes: 30,
  );

  setUp(() {
    taskRepo = MockTaskRepository();
    connectivity = FakeConnectivityService();
    when(() => taskRepo.getTasks()).thenAnswer((_) async => []);
    container = ProviderContainer(
      overrides: [
        taskRepositoryProvider.overrideWithValue(taskRepo),
        connectivityServiceProvider.overrideWithValue(connectivity),
        supabaseClientProvider.overrideWithValue(buildSignedOutSupabaseClient()),
      ],
    );
  });

  tearDown(() => container.dispose());

  final newTaskData = NewTaskData(
    date: DateTime(2026, 7, 24),
    title: 'New task',
    category: TaskCategory.personal,
    difficulty: TaskDifficulty.easy,
    durationMinutes: 15,
  );

  test('addTask replaces the optimistic placeholder with the server task once it lands', () async {
    await container.read(tasksNotifierProvider.notifier).refresh();

    when(() => taskRepo.addTask(newTaskData)).thenAnswer((_) async {});
    when(() => taskRepo.getTasks()).thenAnswer((_) async => [existingTask]);

    await container.read(tasksNotifierProvider.notifier).addTask(newTaskData);

    final state = container.read(tasksNotifierProvider);
    expect(state.tasks, [existingTask]);
    expect(state.tasks.any((t) => t.id.startsWith('pending_')), isFalse);
    expect(state.errorType, isNull);
  });

  test('addTask failure removes the placeholder and sets the create error', () async {
    await container.read(tasksNotifierProvider.notifier).refresh();

    when(() => taskRepo.addTask(newTaskData)).thenThrow(Exception('insert failed'));

    await container.read(tasksNotifierProvider.notifier).addTask(newTaskData);

    final state = container.read(tasksNotifierProvider);
    expect(state.tasks, isEmpty);
    expect(state.errorType, TaskErrorType.create);
  });

  test('toggleTask failure rolls back the optimistic done flag', () async {
    when(() => taskRepo.getTasks()).thenAnswer((_) async => [existingTask]);
    await container.read(tasksNotifierProvider.notifier).refresh();

    when(() => taskRepo.toggleTask(occurrenceId: 'o1', isDone: true)).thenThrow(Exception('toggle failed'));

    await container.read(tasksNotifierProvider.notifier).toggleTask(occurrenceId: 'o1', isDone: true);

    final state = container.read(tasksNotifierProvider);
    expect(state.tasks.single.done, isFalse);
    expect(state.errorType, TaskErrorType.toggle);
  });
}
