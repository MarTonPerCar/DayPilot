/// Mirrors `users` — stats like streaks live in separate tables, fetched
/// elsewhere. Theme and steps goal aren't account-wide columns here anymore
/// (theme is a local-only preference; steps goal lives per-day on
/// habits_daily), so this only carries identity fields.
class AppUser {
  const AppUser({
    required this.id,
    required this.email,
    required this.name,
    required this.username,
    required this.level,
    this.photoUrl,
    this.region,
  });

  final String id;
  final String email;
  final String name;
  final String username;
  final String? photoUrl;
  final String? region;
  final int level;

  factory AppUser.fromMap(Map<String, dynamic> map) {
    return AppUser(
      id: map['id'] as String,
      email: map['email'] as String,
      name: map['name'] as String,
      username: map['username'] as String,
      photoUrl: map['photo_url'] as String?,
      region: map['region'] as String?,
      level: map['level'] as int,
    );
  }
}
