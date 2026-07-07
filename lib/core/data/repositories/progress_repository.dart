import '../models/app_progress.dart';

abstract class ProgressRepository {
  Future<AppProgress> getProgress();

  /// Awards the once-per-day timer point bonus if not already claimed today.
  /// Gated server-side (habits_daily.timer_point_earned) rather than a local
  /// pref, so it's shared correctly across every device/app version.
  /// Returns whether points were newly awarded.
  Future<bool> completeTimerSession();
}
