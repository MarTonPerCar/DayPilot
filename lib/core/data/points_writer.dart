import 'package:supabase_flutter/supabase_flutter.dart';

import '../utils/iso_date.dart';

// Kept the original name (used across the app, including steps_repository.dart
// which isn't being touched right now) even though it no longer checks the
// level itself — fn_notify_level_up (DB trigger) does that automatically now,
// reacting to the chain points_log -> total_points_historical -> level.
Future<void> logPointsAndCheckLevelUp(
  SupabaseClient client, {
  required String userId,
  required int points,
  required String source,
}) async {
  await client.from('points_log').insert({
    'user_id': userId,
    'points': points,
    'source': source,
    'day_key': isoDate(DateTime.now()),
  });
}