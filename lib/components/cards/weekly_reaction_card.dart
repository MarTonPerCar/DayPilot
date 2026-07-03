import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/avatar.dart';
import 'weekly_stats_row.dart';

class WeeklyReaction {
  final String name;
  final String? avatarUrl;
  final String emoji;

  const WeeklyReaction({required this.name, this.avatarUrl, required this.emoji});
}

class WeeklyReactionCard extends StatelessWidget {
  final String weekLabel;
  final int points;
  final int steps;
  final int tasks;
  final int streak;
  final List<WeeklyReaction> reactions;

  const WeeklyReactionCard({
    super.key,
    required this.weekLabel,
    required this.points,
    required this.steps,
    required this.tasks,
    required this.streak,
    this.reactions = const [],
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
            Row(
              children: [
                Icon(Icons.calendar_month_rounded,
                    size: 18, color: colors.onSurfaceVariant),
                const SizedBox(width: 6),
                Text(
                  weekLabel,
                  style: text.labelLarge?.copyWith(
                    color: colors.onSurfaceVariant,
                    fontWeight: FontWeight.w600,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            WeeklyStatsRow(points: points, tasks: tasks, steps: steps, streak: streak),
            if (reactions.isNotEmpty) ...[
              const SizedBox(height: 16),
              Text(
                l10n.weeklyReactionFriendsReactions,
                style: text.labelMedium
                    ?.copyWith(color: colors.onSurfaceVariant),
              ),
              const SizedBox(height: 10),
              Wrap(
                spacing: 16,
                runSpacing: 10,
                children: reactions.map((r) {
                  return Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Stack(
                        clipBehavior: Clip.none,
                        children: [
                          DayPilotAvatar(name: r.name, imageUrl: r.avatarUrl, size: 44),
                          Positioned(
                            bottom: -4,
                            right: -4,
                            child: Container(
                              width: 22,
                              height: 22,
                              alignment: Alignment.center,
                              decoration: BoxDecoration(
                                color: colors.surfaceContainerHighest,
                                shape: BoxShape.circle,
                                border: Border.all(color: colors.surface, width: 2),
                              ),
                              child: Text(r.emoji, style: const TextStyle(fontSize: 12)),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 4),
                      Text(
                        r.name.split(' ').first,
                        style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                      ),
                    ],
                  );
                }).toList(),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
