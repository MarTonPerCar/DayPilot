class AppWeeklySummary {
  const AppWeeklySummary({
    required this.totalPoints,
    required this.totalSteps,
    required this.tasksCompleted,
    required this.bestStreak,
    required this.reactions,
  });

  final int totalPoints;
  final int totalSteps;
  final int tasksCompleted;
  final int bestStreak;
  final List<AppWeeklyReaction> reactions;
}

class AppWeeklyReaction {
  const AppWeeklyReaction({required this.fromName, this.avatarUrl, required this.emoji});

  final String fromName;
  final String? avatarUrl;
  final String emoji;
}
