import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_progress.dart';
import 'progress_repository.dart';

class SupabaseProgressRepository implements ProgressRepository {
  SupabaseProgressRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  // Matches the chart's fixed 30-slot width: 29 closed days from
  // user_daily_log + today's live daily_progress row.
  static const _historyDays = 30;

  @override
  Future<AppProgress> getProgress() async {
    final uid = _userId;
    if (uid == null) {
      return const AppProgress(
        pointsToday: 0,
        pointsFromTasks: 0,
        pointsFromSteps: 0,
        pointsFromHabits: 0,
        pointsFromTimer: 0,
        pointsHistory: [],
        stepsHistory: [],
        tasksHistory: [],
        dayLabels: [],
      );
    }

    final todayRows = await _client.from('daily_progress').select().eq('user_id', uid);
    final today = todayRows.isEmpty ? null : todayRows.first;

    final historyRows = await _client
        .from('user_daily_log')
        .select('date, total_points, steps, tasks_completed')
        .eq('user_id', uid)
        .order('date', ascending: false)
        .limit(_historyDays - 1);
    final history = historyRows.reversed;

    double field(Map<String, dynamic>? row, String key) => (row?[key] as int? ?? 0).toDouble();

    return AppProgress(
      pointsToday: today?['total_points'] as int? ?? 0,
      pointsFromTasks: today?['tasks_points'] as int? ?? 0,
      pointsFromSteps: today?['steps_points'] as int? ?? 0,
      pointsFromHabits: (today?['wellness_points'] as int? ?? 0) + (today?['tech_health_points'] as int? ?? 0),
      pointsFromTimer: today?['timer_points'] as int? ?? 0,
      pointsHistory: [...history.map((row) => field(row, 'total_points')), field(today, 'total_points')],
      stepsHistory: [...history.map((row) => field(row, 'steps')), field(today, 'steps')],
      tasksHistory: [...history.map((row) => field(row, 'tasks_completed')), field(today, 'tasks_completed')],
      dayLabels: [
        ...history.map((row) => DateTime.parse(row['date'] as String).day),
        DateTime.now().day,
      ],
    );
  }
}
