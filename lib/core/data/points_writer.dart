import 'package:supabase_flutter/supabase_flutter.dart';

import '../utils/iso_date.dart';

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
