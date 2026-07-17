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

  final List<int> dayLabels;
}
