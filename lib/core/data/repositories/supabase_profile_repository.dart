import 'dart:typed_data';

import 'package:supabase_flutter/supabase_flutter.dart';

import '../models/app_profile_stats.dart';
import '../models/app_weekly_summary.dart';
import '../reaction_types.dart';
import 'profile_repository.dart';

class SupabaseProfileRepository implements ProfileRepository {
  SupabaseProfileRepository(this._client);

  final SupabaseClient _client;

  String? get _userId => _client.auth.currentUser?.id;

  // Same curve as the `fn_update_level` DB trigger: cumulative points needed
  // to reach level n+1. Used to find where the *current* level started, so
  // the progress bar reflects progress within this level, not since level 1
  // (that flat total/threshold ratio is the known bug in the Android app).
  static int _levelThreshold(int level) => 5 * level * (level + 3);

  @override
  Future<AppProfileStats> getProfileStats() async {
    final uid = _userId;
    if (uid == null) {
      return AppProfileStats(
        name: '',
        username: '',
        email: '',
        createdAt: DateTime.now(),
        region: '',
        level: 1,
        currentXp: 0,
        xpToNextLevel: 1,
        totalPoints: 0,
        streak: 0,
        bestStreak: 0,
      );
    }

    final userRow = await _client
        .from('users')
        .select(
          'name, username, email, created_at, region, photo_url, level, total_points_historical, points_to_next_level',
        )
        .eq('id', uid)
        .single();

    final streakRows = await _client.from('user_streaks').select().eq('user_id', uid);
    final streakRow = streakRows.isEmpty ? null : streakRows.first;

    final level = userRow['level'] as int;
    final totalPoints = userRow['total_points_historical'] as int;
    final nextLevelThreshold = userRow['points_to_next_level'] as int;
    final currentLevelThreshold = _levelThreshold(level - 1);

    return AppProfileStats(
      name: userRow['name'] as String,
      username: userRow['username'] as String,
      email: userRow['email'] as String,
      createdAt: DateTime.parse(userRow['created_at'] as String),
      region: userRow['region'] as String? ?? '',
      avatarUrl: userRow['photo_url'] as String?,
      level: level,
      currentXp: totalPoints - currentLevelThreshold,
      xpToNextLevel: nextLevelThreshold - currentLevelThreshold,
      totalPoints: totalPoints,
      streak: streakRow?['current_streak'] as int? ?? 0,
      bestStreak: streakRow?['longest_streak'] as int? ?? 0,
    );
  }

  @override
  Future<AppWeeklySummary> getWeeklySummary() async {
    final uid = _userId;
    if (uid == null) {
      return const AppWeeklySummary(totalPoints: 0, totalSteps: 0, tasksCompleted: 0, bestStreak: 0, reactions: []);
    }

    final summaryRows = await _client
        .from('user_weekly_summary')
        .select()
        .eq('user_id', uid)
        .order('week_start', ascending: false)
        .limit(1);
    if (summaryRows.isEmpty) {
      return const AppWeeklySummary(totalPoints: 0, totalSteps: 0, tasksCompleted: 0, bestStreak: 0, reactions: []);
    }
    final summary = summaryRows.first;

    final reactionRows =
        await _client.from('reactions').select('from_user_id, type').eq('weekly_summary_id', summary['id']);

    var reactions = const <AppWeeklyReaction>[];
    if (reactionRows.isNotEmpty) {
      final senderIds = reactionRows.map((r) => r['from_user_id'] as String).toList();
      final senders = await _client.from('users').select('id, name, photo_url').inFilter('id', senderIds);
      final senderById = {for (final s in senders) s['id'] as String: s};

      reactions = [
        for (final r in reactionRows)
          if (senderById[r['from_user_id']] case final sender?)
            AppWeeklyReaction(
              fromName: sender['name'] as String,
              avatarUrl: sender['photo_url'] as String?,
              emoji: reactionEmojiByType[r['type'] as String] ?? '🔥',
            ),
      ];
    }

    return AppWeeklySummary(
      totalPoints: summary['total_points'] as int,
      totalSteps: summary['total_steps'] as int,
      tasksCompleted: summary['total_tasks_completed'] as int,
      bestStreak: summary['best_streak'] as int,
      reactions: reactions,
    );
  }

  @override
  Future<void> updateProfile({required String name, required String username, required String region}) async {
    final uid = _userId;
    if (uid == null) return;
    await _client.from('users').update({
      'name': name,
      'username': username,
      'username_lower': username.toLowerCase(),
      'region': region,
    }).eq('id', uid);
  }

  @override
  Future<void> changePassword(String newPassword) async {
    await _client.auth.updateUser(UserAttributes(password: newPassword));
  }

  @override
  Future<String> uploadAvatar({required Uint8List bytes, required String fileExtension}) async {
    final uid = _userId;
    if (uid == null) throw StateError('No authenticated user');

    // Path includes a timestamp so re-uploads get a fresh URL (cached images
    // won't refetch an unchanged URL) — matches the Android app's approach.
    final path = '$uid/${DateTime.now().millisecondsSinceEpoch}.$fileExtension';
    await _client.storage.from('avatars').uploadBinary(
          path,
          bytes,
          fileOptions: const FileOptions(upsert: true),
        );
    final url = _client.storage.from('avatars').getPublicUrl(path);
    await _client.from('users').update({'photo_url': url}).eq('id', uid);
    return url;
  }
}
