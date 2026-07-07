import 'package:supabase_flutter/supabase_flutter.dart';

import 'models/app_notification_item.dart';

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
