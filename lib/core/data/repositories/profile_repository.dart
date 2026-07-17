import 'dart:typed_data';

import '../models/app_profile_stats.dart';
import '../models/app_weekly_summary.dart';

abstract class ProfileRepository {
  Future<AppProfileStats> getProfileStats();

  Future<AppWeeklySummary> getWeeklySummary();

  Future<void> updateProfile({required String name, required String username, required String region});

  Future<void> changePassword(String newPassword);

  Future<String> uploadAvatar({required Uint8List bytes, required String fileExtension});
}
