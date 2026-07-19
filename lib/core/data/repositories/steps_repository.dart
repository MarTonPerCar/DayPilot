import '../models/app_steps.dart';

abstract class StepsRepository {
  Future<AppSteps> getSteps();

  Future<void> setGoal(int newGoal);
}
