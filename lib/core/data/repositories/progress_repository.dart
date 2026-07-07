import '../models/app_progress.dart';

abstract class ProgressRepository {
  Future<AppProgress> getProgress();

  /// Awards the once-per-day timer point bonus, gated server-side. Returns
  /// whether points were newly awarded.
  Future<bool> completeTimerSession();
}
