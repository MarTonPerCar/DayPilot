import 'package:supabase_flutter/supabase_flutter.dart';

import '../../utils/iso_date.dart';
import '../models/app_notification_item.dart';
import '../models/app_steps.dart';
import '../notification_l10n.dart';
import '../notification_writer.dart';
import '../points_writer.dart';
import 'steps_repository.dart';

typedef _Milestone = ({double threshold, int points, int cumulative});

// Gated by the accumulated steps_points column (server-authoritative), not
// an in-memory flag, so it survives restarts and multiple devices.
const _stepMilestones = <_Milestone>[
  (threshold: 0.5, points: 10, cumulative: 10),
  (threshold: 0.75, points: 20, cumulative: 30),
  (threshold: 1.0, points: 30, cumulative: 60),
];

class SupabaseStepsRepository implements StepsRepository {
  SupabaseStepsRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  @override
  Future<AppSteps> getSteps() async {
    final uid = _userId;
    if (uid == null) return const AppSteps(steps: 0, goal: 2000, pointsEarnedToday: 0);

    // pending_steps_goal/_date on `users` is just staging — nothing
    // server-side copies it into habits_daily, so this call is what applies it.
    final today = isoDate(DateTime.now());

    final userRow = await _client
        .from('users')
        .select('pending_steps_goal, pending_steps_goal_date')
        .eq('id', uid)
        .single();
    final pendingGoal = userRow['pending_steps_goal'] as int?;
    final pendingDate = userRow['pending_steps_goal_date'] as String?;
    final pendingIsDue = pendingGoal != null && pendingDate != null && pendingDate.compareTo(today) <= 0;

    final habitsRows =
        await _client.from('habits_daily').select('steps_goal').eq('user_id', uid).eq('date', today);
    var goal = habitsRows.isEmpty ? 2000 : habitsRows.first['steps_goal'] as int;

    if (pendingIsDue) {
      goal = pendingGoal;
      await _client.from('habits_daily').upsert(
        {'user_id': uid, 'date': today, 'steps_goal': goal},
        onConflict: 'user_id, date',
      );
    }

    final progressRows =
        await _client.from('daily_progress').select('steps, steps_points').eq('user_id', uid);
    final progress = progressRows.isEmpty ? null : progressRows.first;
    final steps = progress?['steps'] as int? ?? 0;
    var pointsEarnedToday = progress?['steps_points'] as int? ?? 0;

    if (goal > 0) {
      for (final m in _stepMilestones) {
        if (steps < goal * m.threshold || pointsEarnedToday >= m.cumulative) continue;
        await logPointsAndCheckLevelUp(_client, userId: uid, points: m.points, source: 'STEPS');
        pointsEarnedToday += m.points;
        if (m.cumulative == 60) {
          final l10n = currentL10n();
          await writeNotification(
            _client,
            userId: uid,
            type: AppNotificationType.stepsGoal,
            title: l10n.notifStepsGoalTitle,
            body: l10n.notifStepsGoalBody,
          );
        }
      }
    }

    return AppSteps(
      steps: steps,
      goal: goal,
      pointsEarnedToday: pointsEarnedToday,
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
