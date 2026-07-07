import 'package:supabase_flutter/supabase_flutter.dart';

import '../utils/iso_date.dart';
import 'models/app_notification_item.dart';
import 'notification_l10n.dart';
import 'notification_writer.dart';

/// Level-up check uses two fresh server reads (before/after) instead of a
/// locally-cached level, which is what causes Android's equivalent to
/// re-notify on a second device or fresh install.
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
