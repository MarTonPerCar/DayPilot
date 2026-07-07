class AppFriend {
  const AppFriend({
    required this.friendRowId,
    required this.userId,
    required this.name,
    required this.username,
    this.avatarUrl,
    required this.points,
    required this.streak,
    this.weeklyPoints,
    this.weeklyTasks,
    this.weeklySteps,
    this.weeklyStreak,
    this.weeklySummaryId,
    this.reactionSelected,
  });

  final String friendRowId;
  final String userId;
  final String name;
  final String username;
  final String? avatarUrl;
  final int points;
  final int streak;
  final int? weeklyPoints;
  final int? weeklyTasks;
  final int? weeklySteps;
  final int? weeklyStreak;
  final String? weeklySummaryId;
  final String? reactionSelected;
}

class AppFriendRequest {
  const AppFriendRequest({
    required this.requestId,
    required this.fromUserId,
    required this.name,
    required this.username,
    this.avatarUrl,
  });

  final String requestId;
  final String fromUserId;
  final String name;
  final String username;
  final String? avatarUrl;
}

class AppUserSearchResult {
  const AppUserSearchResult({
    required this.userId,
    required this.name,
    required this.username,
    this.avatarUrl,
    required this.isFriend,
    required this.isPending,
  });

  final String userId;
  final String name;
  final String username;
  final String? avatarUrl;
  final bool isFriend;
  final bool isPending;
}
