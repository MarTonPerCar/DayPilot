import '../models/app_progress.dart';

abstract class ProgressRepository {
  Future<AppProgress> getProgress();

  Future<bool> completeTimerSession();
}
