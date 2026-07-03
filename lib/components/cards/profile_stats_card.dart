import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/avatar.dart';
import '../basic/stat_tile.dart';

class ProfileStatsCard extends StatelessWidget {
  final String name;
  final String username;
  final String? avatarUrl;
  final int level;
  final int currentXp;
  final int xpToNextLevel;
  final int totalPoints;
  final int streak;
  final int bestStreak;

  const ProfileStatsCard({
    super.key,
    required this.name,
    required this.username,
    this.avatarUrl,
    required this.level,
    required this.currentXp,
    required this.xpToNextLevel,
    required this.totalPoints,
    required this.streak,
    required this.bestStreak,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final progress = xpToNextLevel > 0 ? (currentXp / xpToNextLevel).clamp(0.0, 1.0) : 0.0;

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                DayPilotAvatar(name: name, imageUrl: avatarUrl, size: 64),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        name,
                        style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700),
                      ),
                      Text(
                        '@$username',
                        style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
                      ),
                      const SizedBox(height: 6),
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                        decoration: BoxDecoration(
                          color: colors.primaryContainer,
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Text('⚡', style: TextStyle(fontSize: 12)),
                            const SizedBox(width: 4),
                            Text(
                              l10n.profileLevelBadge(level),
                              style: text.labelMedium?.copyWith(
                                fontWeight: FontWeight.w700,
                                color: colors.onPrimaryContainer,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  l10n.profileLevelProgress(level + 1),
                  style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700),
                ),
                Text(
                  l10n.profileXpProgress(currentXp, xpToNextLevel),
                  style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                ),
              ],
            ),
            const SizedBox(height: 8),
            ClipRRect(
              borderRadius: BorderRadius.circular(8),
              child: LinearProgressIndicator(
                value: progress,
                minHeight: 8,
                backgroundColor: colors.surfaceContainerHighest,
                color: colors.primary,
              ),
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                StatTile(
                  icon: Icons.star_rounded,
                  color: const Color(0xFFFFD700),
                  label: l10n.profileTotalPoints,
                  value: _formatPoints(totalPoints),
                ),
                const SizedBox(width: 12),
                StatTile(
                  icon: Icons.local_fire_department_rounded,
                  color: colors.error,
                  label: l10n.profileCurrentStreak,
                  value: '$streak',
                ),
                const SizedBox(width: 12),
                StatTile(
                  icon: Icons.emoji_events_rounded,
                  color: colors.tertiary,
                  label: l10n.profileBestStreak,
                  value: '$bestStreak',
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _formatPoints(int pts) {
    if (pts >= 1000) return '${(pts / 1000).toStringAsFixed(1)}k';
    return '$pts';
  }
}
