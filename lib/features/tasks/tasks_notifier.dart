import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';

import '../../core/cache/session_cache.dart';
import '../../core/data/models/app_task.dart';
import '../../core/data/models/task_category.dart';
import '../../core/data/models/task_difficulty.dart';
import '../../core/data/repositories/providers.dart';
import '../progress/progress_notifier.dart';
import 'task_error.dart';
import 'tasks_state.dart';

class TasksNotifier extends Notifier<TasksState> {
  RealtimeChannel? _channel;
  bool _refreshing = false;

  @override
  TasksState build() {
    Future.microtask(_load);
    ref.onDispose(() => _channel?.unsubscribe());
    return const TasksState(isLoading: true);
  }

  Future<void> _load() async {
    try {
      final tasks = await ref.read(taskRepositoryProvider).getTasks();
      state = state.copyWith(tasks: tasks, isLoading: false);
      _subscribeToRealtimeOnce();
    } catch (_) {
      state = state.copyWith(isLoading: false);
    }
  }

  void _subscribeToRealtimeOnce() {
    if (_channel != null) return;
    final uid = ref.read(supabaseClientProvider).auth.currentUser?.id;
    if (uid == null) return;

    _channel = ref
        .read(supabaseClientProvider)
        .channel('tasks-$uid')
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'tasks',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'user_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .onPostgresChanges(
          event: PostgresChangeEvent.all,
          schema: 'public',
          table: 'task_days',
          filter: PostgresChangeFilter(type: PostgresChangeFilterType.eq, column: 'user_id', value: uid),
          callback: (payload) => _refreshFromRealtime(),
        )
        .subscribe();
  }

  Future<void> _refreshFromRealtime() async {
    if (_refreshing) return;
    _refreshing = true;
    try {
      ref.invalidate(tasksCacheProvider);
      await _load();
    } finally {
      _refreshing = false;
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
      if (isDone) await ref.read(progressNotifierProvider.notifier).refresh();
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
