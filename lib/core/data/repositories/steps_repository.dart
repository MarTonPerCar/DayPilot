import '../models/app_steps.dart';

abstract class StepsRepository {
  Future<AppSteps> getSteps();

  /// Takes effect tomorrow — applied server-side by a nightly job, not by
  /// any client, so it's the same for every device.
  Future<void> setGoal(int newGoal);
}
