import '../models/app_tech_restriction.dart';

abstract class TechHealthRepository {
  Future<List<AppTechRestriction>> getRestrictions();

  Future<void> saveRestriction({
    required String appPackage,
    required String appName,
    required int limitMinutes,
  });

  Future<void> toggleRestriction(String appPackage, bool isActive);

  /// Soft delete — Android's own nightly cron (fn_close_daily_progress) does
  /// the actual DELETE the next day, matching the "deletes tomorrow" UI copy.
  Future<void> deleteRestriction(String appPackage);

  /// Whether today's tech-health bonus is still on track (no active
  /// restriction violated yet). True with no restrictions/violations at all
  /// — a clean day, not a violated one.
  Future<bool> getPointEarnedToday();
}
