import 'package:supabase_flutter/supabase_flutter.dart';

import '../utils/iso_date.dart';
import 'models/app_notification_item.dart';
import 'notification_l10n.dart';
import 'notification_writer.dart';

/// Inserts a points_log row and notifies the user if this specific award
/// crosses a level boundary.
///
/// The level check is done with two fresh server reads (before/after) rather
/// than a locally-remembered "last known level" — the points_log -> users
/// trigger chain (fn_sync_points_to_progress -> fn_update_level) runs
/// synchronously within the insert, so users.level is already current by the
/// time this returns. A locally-cached level would need to survive
/// reinstalls/other devices to avoid re-notifying for the same level-up,
/// which is the actual bug in Android's equivalent (DayPilotNavGraph.kt
/// compares against a SharedPreferences value, so a second device or a fresh
/// install re-fires the notification for an already-reached level).
Future<void> logPointsAndCheckLevelUp(
  SupabaseClient client, {
  required String userId,
  required int points,
  required String source,
}) async {
  final levelBefore = await _readLevel(client, userId);

  await client.from('points_log').insert({
    'user_id': userId,
    'points': points,
    'source': source,
    'day_key': isoDate(DateTime.now()),
  });

  final levelAfter = await _readLevel(client, userId);
  if (levelAfter <= levelBefore) return;

  final l10n = currentL10n();
  await writeNotification(
    client,
    userId: userId,
    type: AppNotificationType.levelUp,
    title: l10n.notifLevelUpTitle,
    body: l10n.notifLevelUpBody(levelAfter),
  );
}

Future<int> _readLevel(SupabaseClient client, String userId) async {
  final row = await client.from('users').select('level').eq('id', userId).single();
  return row['level'] as int;
}
