import 'package:supabase_flutter/supabase_flutter.dart';

import '../../utils/iso_date.dart';
import '../models/app_steps.dart';
import 'steps_repository.dart';

class SupabaseStepsRepository implements StepsRepository {
  SupabaseStepsRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<AppSteps> getSteps() async {
    final uid = _userId;
    if (uid == null) return const AppSteps(steps: 0, goal: 2000, pointsEarnedToday: 0);

    final userRow = await _client
        .from('users')
        .select('default_steps_goal, pending_steps_goal')
        .eq('id', uid)
        .single();

    final progressRows =
        await _client.from('daily_progress').select('steps, steps_points').eq('user_id', uid);
    final progress = progressRows.isEmpty ? null : progressRows.first;

    return AppSteps(
      steps: progress?['steps'] as int? ?? 0,
      goal: userRow['default_steps_goal'] as int,
      pointsEarnedToday: progress?['steps_points'] as int? ?? 0,
      pendingGoal: userRow['pending_steps_goal'] as int?,
    );
  }

  @override
  Future<void> setGoal(int newGoal) async {
    final uid = _userId;
    if (uid == null) return;
    final tomorrow = DateTime.now().add(const Duration(days: 1));
    await _client.from('users').update({
      'pending_steps_goal': newGoal,
      'pending_steps_goal_date': isoDate(tomorrow),
    }).eq('id', uid);
  }
}
