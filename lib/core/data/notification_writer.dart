import 'package:supabase_flutter/supabase_flutter.dart';

import 'models/app_notification_item.dart';

/// Inserts a notification row for [userId] — used by other repositories as a
/// side effect of their own actions (e.g. sending a friend request notifies
/// the recipient). `notifications_insert_auth` lets any authenticated user
/// insert a row for any user_id, since there's no DB trigger for this yet.
Future<void> writeNotification(
  SupabaseClient client, {
  required String userId,
  required AppNotificationType type,
  required String title,
  required String body,
}) async {
  await client.from('notifications').insert({
    'user_id': userId,
    'type': type.dbValue,
    'title': title,
    'body': body,
  });
}
