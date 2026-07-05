import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

class DailySummaryCard extends StatelessWidget {
  final String userName;
  final int streak;
  final int stepsToday;
  final int stepsGoal;
  final int tasksCompleted;
  final int tasksTotal;
  final int pointsToday;
  final int rankingPosition;

  const DailySummaryCard({
    super.key,
    required this.userName,
    this.streak = 0,
    this.stepsToday = 0,
    this.stepsGoal = 10000,
    this.tasksCompleted = 0,
    this.tasksTotal = 0,
    this.pointsToday = 0,
    this.rankingPosition = 0,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Card(
      elevation: 4,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(24)),
      ),
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Container(
        decoration: BoxDecoration(
          gradient: LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [
              colors.primary.withAlpha(31),
              colors.tertiary.withAlpha(15),
            ],
          ),
        ),
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        l10n.dailySummaryGreeting(userName),
                        style: text.headlineSmall?.copyWith(
                          fontWeight: FontWeight.w700,
                          color: colors.onSurface,
                        ),
                      ),
                      Text(
                        l10n.dailySummarySubtitle,
                        style: text.bodySmall?.copyWith(
                          color: colors.onSurfaceVariant,
                        ),
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                  decoration: BoxDecoration(
                    color: colors.primaryContainer,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Text('🔥', style: TextStyle(fontSize: 13)),
                      const SizedBox(width: 4),
                      Text(
                        l10n.dailySummaryStreakDays(streak),
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

            const Spacer(),

            Row(
              children: [
                Expanded(
                  child: _StatCell(
                    icon: Icons.directions_walk_rounded,
                    label: l10n.commonSteps,
                    value: _fmt(stepsToday),
                    sublabel: l10n.dailySummaryGoalCaption(_fmt(stepsGoal)),
                    color: colors.primary,
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _StatCell(
                    icon: Icons.task_alt_rounded,
                    label: l10n.commonTasks,
                    value: '$tasksCompleted/$tasksTotal',
                    sublabel: l10n.dailySummaryCompleted,
                    color: colors.secondary,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Expanded(
                  child: _StatCell(
                    icon: Icons.star_rounded,
                    label: l10n.commonPointsToday,
                    value: '+$pointsToday',
                    sublabel: l10n.dailySummaryPointsEarnedLabel,
                    color: const Color(0xFFFFAA00),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: _StatCell(
                    icon: Icons.emoji_events_rounded,
                    label: l10n.commonRanking,
                    value: '#$rankingPosition',
                    sublabel: l10n.dailySummaryAmongFriends,
                    color: colors.tertiary,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  static String _fmt(int n) =>
      n >= 1000 ? '${(n / 1000).toStringAsFixed(1)}k' : '$n';
}

class _StatCell extends StatelessWidget {
  final IconData icon;
  final String label;
  final String value;
  final String sublabel;
  final Color color;

  const _StatCell({
    required this.icon,
    required this.label,
    required this.value,
    required this.sublabel,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: colors.surface.withAlpha(190),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(icon, size: 13, color: color),
              const SizedBox(width: 4),
              Expanded(
                child: Text(
                  label,
                  style: text.labelSmall?.copyWith(
                    color: colors.onSurfaceVariant,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ),
            ],
          ),
          const SizedBox(height: 3),
          Text(
            value,
            style: text.titleMedium?.copyWith(
              fontWeight: FontWeight.w700,
              color: colors.onSurface,
            ),
          ),
          Text(
            sublabel,
            style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    );
  }
}
