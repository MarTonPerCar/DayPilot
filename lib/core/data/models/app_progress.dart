class AppProgress {
  const AppProgress({
    required this.pointsToday,
    required this.pointsFromTasks,
    required this.pointsFromSteps,
    required this.pointsFromHabits,
    required this.pointsFromTimer,
    required this.pointsHistory,
    required this.stepsHistory,
    required this.tasksHistory,
    required this.dayLabels,
  });

  final int pointsToday;
  final int pointsFromTasks;
  final int pointsFromSteps;
  final int pointsFromHabits;
  final int pointsFromTimer;
  final List<double> pointsHistory;
  final List<double> stepsHistory;
  final List<double> tasksHistory;

  /// Calendar day-of-month for each entry in the history lists above (same
  /// length, same order) — lets the chart label the x-axis with real dates.
  final List<int> dayLabels;
}
