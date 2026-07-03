import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/stat_tile.dart';

class DailyPointsCard extends StatelessWidget {
  final int rankingPosition;
  final int pointsToday;
  final int pointsFromTasks;
  final int pointsFromSteps;
  final int pointsFromHabits;
  final int pointsFromTimer;

  const DailyPointsCard({
    super.key,
    required this.rankingPosition,
    required this.pointsToday,
    this.pointsFromTasks = 0,
    this.pointsFromSteps = 0,
    this.pointsFromHabits = 0,
    this.pointsFromTimer = 0,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              l10n.dailySummaryTitle,
              style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                StatTile(
                  icon: Icons.emoji_events_rounded,
                  color: colors.tertiary,
                  value: '#$rankingPosition',
                  label: l10n.commonRanking,
                ),
                const SizedBox(width: 12),
                StatTile(
                  icon: Icons.star_rounded,
                  color: const Color(0xFFFFD700),
                  value: '$pointsToday',
                  label: l10n.commonPointsToday,
                ),
              ],
            ),
            const SizedBox(height: 16),
            _SourceRow(icon: Icons.task_alt_rounded, label: l10n.commonTasks, color: colors.primary, points: pointsFromTasks),
            _SourceRow(icon: Icons.directions_walk_rounded, label: l10n.commonSteps, color: colors.tertiary, points: pointsFromSteps),
            _SourceRow(icon: Icons.favorite_rounded, label: l10n.commonHabits, color: colors.secondary, points: pointsFromHabits),
            _SourceRow(icon: Icons.timer_rounded, label: l10n.commonTimer, color: colors.error, points: pointsFromTimer),
          ],
        ),
      ),
    );
  }
}

class _SourceRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color color;
  final int points;

  const _SourceRow({
    required this.icon,
    required this.label,
    required this.color,
    required this.points,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(
        children: [
          Icon(icon, size: 18, color: color),
          const SizedBox(width: 10),
          Expanded(
            child: Text(label, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
          ),
          Text(
            l10n.commonPointsSuffix(points),
            style: text.bodyMedium?.copyWith(fontWeight: FontWeight.w600),
          ),
        ],
      ),
    );
  }
}
