import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../core/cache/session_cache.dart';
import '../../core/data/models/app_task.dart';
import '../../core/data/models/task_category.dart';
import '../../core/data/models/task_difficulty.dart';
import '../../core/data/repositories/providers.dart';
import 'task_error.dart';
import 'tasks_state.dart';

class TasksNotifier extends Notifier<TasksState> {
  @override
  TasksState build() {
    // Deferred: `state` can't be touched synchronously inside build() itself,
    // only once the provider has actually finished initializing.
    Future.microtask(_load);
    return const TasksState(isLoading: true);
  }

  Future<void> _load() async {
    try {
      final tasks = await ref.read(taskRepositoryProvider).getTasks();
      state = state.copyWith(tasks: tasks, isLoading: false);
    } catch (_) {
      state = state.copyWith(isLoading: false);
    }
  }

  Future<void> refresh() => _load();

  Future<void> addTask(NewTaskData data) async {
    final placeholderId = 'pending_${DateTime.now().microsecondsSinceEpoch}';
    final placeholder = AppTask(
      id: placeholderId,
      occurrenceId: placeholderId,
      title: data.title,
      description: data.description.isEmpty ? null : data.description,
      difficulty: data.difficulty,
      category: data.category,
      date: data.date,
      durationMinutes: data.durationMinutes,
      reminder: data.reminder,
      recurring: data.recurring,
    );
    state = state.copyWith(tasks: [...state.tasks, placeholder]);
    try {
      await ref.read(taskRepositoryProvider).addTask(data);
      // Recurring tasks create several rows with server-assigned ids that
      // the placeholder can't represent, so refetch instead of patching it.
      ref.invalidate(tasksCacheProvider);
      await _load();
    } catch (_) {
      state = state.copyWith(
        tasks: state.tasks.where((t) => t.id != placeholderId).toList(),
        errorType: TaskErrorType.create,
      );
    }
  }

  Future<void> updateTask({
    required String id,
    required String title,
    required String description,
    required TaskCategory category,
    required TaskDifficulty difficulty,
    required int durationMinutes,
  }) async {
    final previous = state.tasks;
    state = state.copyWith(
      tasks: [
        for (final t in state.tasks)
          if (t.id == id)
            t.copyWith(
              title: title,
              description: description,
              category: category,
              difficulty: difficulty,
              durationMinutes: durationMinutes,
            )
          else
            t,
      ],
    );
    try {
      await ref.read(taskRepositoryProvider).updateTask(
            id: id,
            title: title,
            description: description,
            category: category,
            difficulty: difficulty,
            durationMinutes: durationMinutes,
          );
      ref.read(tasksCacheProvider.notifier).state = state.tasks;
    } catch (_) {
      state = state.copyWith(tasks: previous, errorType: TaskErrorType.update);
    }
  }

  Future<void> toggleTask({required String occurrenceId, required bool isDone}) async {
    final previous = state.tasks;
    state = state.copyWith(
      tasks: [
        for (final t in state.tasks)
          if (t.occurrenceId == occurrenceId) t.copyWith(done: isDone) else t,
      ],
    );
    try {
      await ref.read(taskRepositoryProvider).toggleTask(occurrenceId: occurrenceId, isDone: isDone);
      ref.read(tasksCacheProvider.notifier).state = state.tasks;
    } catch (_) {
      state = state.copyWith(tasks: previous, errorType: TaskErrorType.toggle);
    }
  }

  Future<void> deleteTask(String id) async {
    final previous = state.tasks;
    state = state.copyWith(tasks: state.tasks.where((t) => t.id != id).toList());
    try {
      await ref.read(taskRepositoryProvider).deleteTask(id);
      ref.read(tasksCacheProvider.notifier).state = state.tasks;
    } catch (_) {
      state = state.copyWith(tasks: previous, errorType: TaskErrorType.delete);
    }
  }

  void clearError() {
    state = state.copyWith(errorType: null);
  }
}

final tasksNotifierProvider = NotifierProvider<TasksNotifier, TasksState>(TasksNotifier.new);
