/// Mirrors `users` (see `01_schema_v004.sql`) — stats like streaks live in
/// separate tables, fetched elsewhere.
class AppUser {
  const AppUser({
    required this.id,
    required this.email,
    required this.name,
    required this.username,
    required this.level,
    required this.themeColor,
    required this.defaultStepsGoal,
    this.photoUrl,
    this.region,
    this.zoneId,
  });

  final String id;
  final String email;
  final String name;
  final String username;
  final String? photoUrl;
  final String? region;
  final String? zoneId;
  final int level;
  final String themeColor;
  final int defaultStepsGoal;

  factory AppUser.fromMap(Map<String, dynamic> map) {
    return AppUser(
      id: map['id'] as String,
      email: map['email'] as String,
      name: map['name'] as String,
      username: map['username'] as String,
      photoUrl: map['photo_url'] as String?,
      region: map['region'] as String?,
      zoneId: map['zone_id'] as String?,
      level: map['level'] as int,
      themeColor: map['theme_color'] as String,
      defaultStepsGoal: map['default_steps_goal'] as int,
    );
  }
}
