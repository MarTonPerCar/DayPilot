import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/avatar.dart';
import '../basic/reactions.dart';
import 'stats_pill.dart';
import 'weekly_stats_row.dart';

class FriendCard extends StatelessWidget {
  final String name;
  final String email;
  final String? avatarUrl;
  final int points;
  final int streak;
  final int? weeklyPoints;
  final int? weeklyTasks;
  final int? weeklySteps;
  final int? weeklyStreak;
  final String? reactionSelected;
  final void Function(String)? onReact;
  final VoidCallback? onRemove;
  final VoidCallback? onTap;

  const FriendCard({
    super.key,
    required this.name,
    required this.email,
    this.avatarUrl,
    this.points = 0,
    this.streak = 0,
    this.weeklyPoints,
    this.weeklyTasks,
    this.weeklySteps,
    this.weeklyStreak,
    this.reactionSelected,
    this.onReact,
    this.onRemove,
    this.onTap,
  });

  bool get _hasWeeklySummary =>
      weeklyPoints != null ||
      weeklyTasks != null ||
      weeklySteps != null ||
      weeklyStreak != null;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Card.filled(
      clipBehavior: Clip.antiAlias,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  DayPilotAvatar(name: name, imageUrl: avatarUrl, size: 48),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          name,
                          style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w700),
                        ),
                        Text(
                          email,
                          style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: Icon(Icons.person_remove_rounded, color: colors.error, size: 20),
                    onPressed: onRemove,
                    tooltip: l10n.friendCardRemoveTooltip,
                  ),
                ],
              ),
              const SizedBox(height: 12),
              StatsPill(points: points, streak: streak),
              if (_hasWeeklySummary) ...[
                const SizedBox(height: 14),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.center,
                  children: [
                    Expanded(
                      child: WeeklyStatsRow(
                        points: weeklyPoints ?? 0,
                        tasks: weeklyTasks ?? 0,
                        steps: weeklySteps ?? 0,
                        streak: weeklyStreak ?? 0,
                      ),
                    ),
                    if (onReact != null) ...[
                      const SizedBox(width: 8),
                      DayPilotReactionPicker(selected: reactionSelected, onReact: onReact!),
                    ],
                  ],
                ),
              ] else ...[
                const SizedBox(height: 10),
                Text(
                  l10n.friendCardNoActivity,
                  style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}

class FriendRequestCard extends StatelessWidget {
  final String name;
  final String email;
  final String? avatarUrl;
  final VoidCallback? onAccept;
  final VoidCallback? onDecline;

  const FriendRequestCard({
    super.key,
    required this.name,
    required this.email,
    this.avatarUrl,
    this.onAccept,
    this.onDecline,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Card.filled(
      clipBehavior: Clip.antiAlias,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Row(
          children: [
            DayPilotAvatar(name: name, imageUrl: avatarUrl, size: 48),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    name,
                    style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w700),
                  ),
                  Text(
                    email,
                    style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                  ),
                ],
              ),
            ),
            IconButton(
              icon: Icon(Icons.close_rounded, color: colors.error),
              onPressed: onDecline,
              tooltip: l10n.friendCardDecline,
            ),
            FilledButton(
              onPressed: onAccept,
              child: Text(l10n.commonAccept),
            ),
          ],
        ),
      ),
    );
  }
}

class UserSearchCard extends StatelessWidget {
  final String name;
  final String email;
  final String? avatarUrl;
  final bool isFriend;
  final bool isPending;
  final VoidCallback? onAdd;
  final VoidCallback? onTap;

  const UserSearchCard({
    super.key,
    required this.name,
    required this.email,
    this.avatarUrl,
    this.isFriend = false,
    this.isPending = false,
    this.onAdd,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return Card.filled(
      clipBehavior: Clip.antiAlias,
      margin: EdgeInsets.zero,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              DayPilotAvatar(name: name, imageUrl: avatarUrl, size: 44),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      name,
                      style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w700),
                    ),
                    Text(
                      email,
                      style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                    ),
                  ],
                ),
              ),
              if (isFriend)
                Icon(Icons.people_rounded, color: colors.primary)
              else if (isPending)
                Text(
                  l10n.friendCardPending,
                  style: text.labelMedium?.copyWith(color: colors.onSurfaceVariant),
                )
              else
                FilledButton.tonal(
                  onPressed: onAdd,
                  child: Text(l10n.commonAdd),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
