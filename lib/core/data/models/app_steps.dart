class AppSteps {
  const AppSteps({
    required this.steps,
    required this.goal,
    required this.pointsEarnedToday,
    this.pendingGoal,
  });

  final int steps;
  final int goal;
  final int pointsEarnedToday;
  final int? pendingGoal;

  bool get hasPendingGoalChange => pendingGoal != null;
}
