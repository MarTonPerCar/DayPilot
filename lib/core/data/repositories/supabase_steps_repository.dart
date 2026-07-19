import 'package:supabase_flutter/supabase_flutter.dart';

import '../../utils/iso_date.dart';
import '../models/app_steps.dart';
import 'steps_repository.dart';

class SupabaseStepsRepository implements StepsRepository {
  SupabaseStepsRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  static const _pointsByMilestoneLevel = [0, 10, 30, 60];

  @override
  Future<AppSteps> getSteps() async {
    final uid = _userId;
    if (uid == null) return const AppSteps(steps: 0, goal: 10000, pointsEarnedToday: 0);
    final today = isoDate(DateTime.now());

    final userRow = await _client
        .from('users')
        .select('pending_steps_goal, pending_steps_goal_date')
        .eq('id', uid)
        .single();
    final pendingGoal = userRow['pending_steps_goal'] as int?;
    final pendingDate = userRow['pending_steps_goal_date'] as String?;
    final pendingIsDue = pendingGoal != null && pendingDate != null && pendingDate.compareTo(today) <= 0;

    final habitsRows = await _client
        .from('habits_daily')
        .select('steps, steps_goal, steps_milestone_level')
        .eq('user_id', uid)
        .eq('date', today);
    final habitsRow = habitsRows.isEmpty ? null : habitsRows.first;
    var goal = habitsRow == null ? 10000 : habitsRow['steps_goal'] as int;
    final steps = habitsRow?['steps'] as int? ?? 0;
    final milestoneLevel = habitsRow?['steps_milestone_level'] as int? ?? 0;

    if (pendingIsDue) {
      goal = pendingGoal;
      await _client.from('habits_daily').upsert(
        {'user_id': uid, 'date': today, 'steps_goal': goal},
        onConflict: 'user_id, date',
      );
    }

    return AppSteps(
      steps: steps,
      goal: goal,
      pointsEarnedToday: _pointsByMilestoneLevel[milestoneLevel],
      pendingGoal: pendingIsDue ? null : pendingGoal,
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
