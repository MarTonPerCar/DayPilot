class AppRankingEntry {
  const AppRankingEntry({
    required this.userId,
    required this.name,
    required this.username,
    this.avatarUrl,
    required this.points,
    required this.streak,
    required this.isCurrentUser,
  });

  final String userId;
  final String name;
  final String username;
  final String? avatarUrl;
  final int points;
  final int streak;
  final bool isCurrentUser;
}
