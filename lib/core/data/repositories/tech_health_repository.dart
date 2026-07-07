import '../models/app_tech_restriction.dart';

abstract class TechHealthRepository {
  Future<List<AppTechRestriction>> getRestrictions();

  Future<void> saveRestriction({
    required String appPackage,
    required String appName,
    required int limitMinutes,
  });

  Future<void> toggleRestriction(String appPackage, bool isActive);

  /// Soft delete — the nightly cron does the actual DELETE the next day.
  Future<void> deleteRestriction(String appPackage);

  Future<bool> getPointEarnedToday();
}
