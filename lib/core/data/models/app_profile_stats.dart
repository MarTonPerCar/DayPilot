class AppProfileStats {
  const AppProfileStats({
    required this.name,
    required this.username,
    required this.email,
    required this.createdAt,
    required this.region,
    this.avatarUrl,
    required this.level,
    required this.currentXp,
    required this.xpToNextLevel,
    required this.totalPoints,
    required this.streak,
    required this.bestStreak,
  });

  final String name;
  final String username;
  final String email;
  final DateTime createdAt;
  final String region;
  final String? avatarUrl;
  final int level;

  /// Points earned within the current level (not since level 1).
  final int currentXp;

  /// Size of the current level's span (not the absolute next-level threshold).
  final int xpToNextLevel;
  final int totalPoints;
  final int streak;
  final int bestStreak;
}
