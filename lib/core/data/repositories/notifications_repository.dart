import '../models/app_notification_item.dart';

abstract class NotificationsRepository {
  Future<List<AppNotificationItem>> getNotifications();

  Future<void> markAsRead(String id);

  Future<void> markAllAsRead();
}
