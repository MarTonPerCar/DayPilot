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

  final int currentXp;

  final int xpToNextLevel;
  final int totalPoints;
  final int streak;
  final int bestStreak;
}
