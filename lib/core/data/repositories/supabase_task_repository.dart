import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:supabase_flutter/supabase_flutter.dart';
import 'package:uuid/uuid.dart';

import '../../cache/session_cache.dart';
import '../models/app_task.dart';
import '../models/task_category.dart';
import '../models/task_difficulty.dart';
import 'task_repository.dart';

class SupabaseTaskRepository implements TaskRepository {
  SupabaseTaskRepository(this._client, this._ref);

  final SupabaseClient _client;
  final Ref _ref;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<List<AppTask>> getTasks() async {
    final cached = _ref.read(tasksCacheProvider);
    if (cached != null) return cached.cast<AppTask>();

    final uid = _userId;
    if (uid == null) return [];
    final rows = await _client.from('calendar_tasks').select().eq('user_id', uid);
    final tasks = rows.map(AppTask.fromMap).toList();
    _ref.read(tasksCacheProvider.notifier).state = tasks;
    return tasks;
  }

  @override
  Future<void> addTask(NewTaskData data) async {
    final uid = _userId;
    if (uid == null) return;
    final taskId = const Uuid().v4();

    await _client.from('tasks').insert({
      'id': taskId,
      'user_id': uid,
      'title': data.title,
      'description': data.description.isEmpty ? null : data.description,
      'category': taskCategoryToDb(data.category),
      'difficulty': data.difficulty.name.toUpperCase(),
      'estimated_minutes': data.durationMinutes,
      'reminder_enabled': data.reminder,
      'is_recurring': data.recurring,
    });

    await _client.from('task_days').insert({
      'task_id': taskId,
      'user_id': uid,
      'date': _isoDate(data.date),
    });

    // One row per occurrence, capped 90 days out — same approach as Android;
    // worth a DB function instead of N round-trips if this gets slow.
    if (data.recurring && data.recurrenceDays >= 1) {
      final limit = data.date.add(const Duration(days: 90));
      var next = data.date.add(Duration(days: data.recurrenceDays));
      while (!next.isAfter(limit)) {
        await _client.from('task_days').insert({
          'task_id': taskId,
          'user_id': uid,
          'date': _isoDate(next),
        });
        next = next.add(Duration(days: data.recurrenceDays));
      }
    }
  }

  @override
  Future<void> updateTask({
    required String id,
    required String title,
    required String description,
    required TaskCategory category,
    required TaskDifficulty difficulty,
    required int durationMinutes,
  }) async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('tasks').update({
      'title': title,
      'description': description.isEmpty ? null : description,
      'category': taskCategoryToDb(category),
      'difficulty': difficulty.name.toUpperCase(),
      'estimated_minutes': durationMinutes,
    }).eq('id', id).eq('user_id', uid);
  }

  @override
  Future<void> toggleTask({required String occurrenceId, required bool isDone}) async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('task_days').update({
      'is_completed': isDone,
      'completed_at': isDone ? DateTime.now().toUtc().toIso8601String() : null,
    }).eq('id', occurrenceId).eq('user_id', uid);
  }

  @override
  Future<void> deleteTask(String id) async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('tasks').delete().eq('id', id).eq('user_id', uid);
  }
}

String _isoDate(DateTime date) =>
    '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
