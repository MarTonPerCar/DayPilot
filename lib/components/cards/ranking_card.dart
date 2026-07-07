import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/avatar.dart';

class RankingCard extends StatelessWidget {
  final int position;
  final String username;
  final String? avatarUrl;
  final int points;
  final int streak;
  final bool isCurrentUser;

  const RankingCard({
    super.key,
    required this.position,
    required this.username,
    this.avatarUrl,
    required this.points,
    this.streak = 0,
    this.isCurrentUser = false,
  });

  Color _positionColor(ColorScheme colors) => switch (position) {
        1 => const Color(0xFFFFD700),
        2 => const Color(0xFFC0C0C0),
        3 => const Color(0xFFCD7F32),
        _ => colors.onSurfaceVariant,
      };

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    final l10n = AppLocalizations.of(context);

    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      margin: const EdgeInsets.symmetric(vertical: 3),
      decoration: BoxDecoration(
        color: isCurrentUser
            ? colors.primaryContainer.withAlpha(100)
            : colors.surfaceContainerLow,
        borderRadius: BorderRadius.circular(12),
        border: isCurrentUser
            ? Border.all(color: colors.primary.withAlpha(100))
            : null,
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      child: Row(
        children: [
          SizedBox(
            width: 28,
            child: Text(
              '$position',
              style: text.titleMedium?.copyWith(
                fontWeight: FontWeight.w800,
                color: _positionColor(colors),
              ),
              textAlign: TextAlign.center,
            ),
          ),
          const SizedBox(width: 12),
          DayPilotAvatar(name: username, imageUrl: avatarUrl, size: 40),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  username,
                  style: text.bodyLarge?.copyWith(
                    fontWeight: isCurrentUser ? FontWeight.w600 : FontWeight.w400,
                    color: colors.onSurface,
                  ),
                ),
                if (streak > 0)
                  Row(
                    children: [
                      Icon(Icons.local_fire_department_rounded,
                          size: 14, color: colors.error),
                      const SizedBox(width: 2),
                      Text(
                        l10n.dailySummaryStreakDays(streak),
                        style: text.labelSmall
                            ?.copyWith(color: colors.onSurfaceVariant),
                      ),
                    ],
                  ),
              ],
            ),
          ),
          Text(
            '$points pts',
            style: text.titleSmall?.copyWith(
              fontWeight: FontWeight.w700,
              color: colors.primary,
            ),
          ),
        ],
      ),
    );
  }
}

class PodiumCard extends StatelessWidget {
  final String firstName;
  final String? firstAvatar;
  final int firstPoints;
  final String secondName;
  final String? secondAvatar;
  final int secondPoints;
  final String thirdName;
  final String? thirdAvatar;
  final int thirdPoints;

  const PodiumCard({
    super.key,
    required this.firstName,
    this.firstAvatar,
    required this.firstPoints,
    required this.secondName,
    this.secondAvatar,
    required this.secondPoints,
    required this.thirdName,
    this.thirdAvatar,
    required this.thirdPoints,
  });

  @override
  Widget build(BuildContext context) {
    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 20, 16, 0),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceEvenly,
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            _PodiumSlot(
              position: 2,
              name: secondName,
              avatarUrl: secondAvatar,
              points: secondPoints,
              barHeight: 64,
              medalColor: const Color(0xFFC0C0C0),
            ),
            _PodiumSlot(
              position: 1,
              name: firstName,
              avatarUrl: firstAvatar,
              points: firstPoints,
              barHeight: 96,
              medalColor: const Color(0xFFFFD700),
            ),
            _PodiumSlot(
              position: 3,
              name: thirdName,
              avatarUrl: thirdAvatar,
              points: thirdPoints,
              barHeight: 44,
              medalColor: const Color(0xFFCD7F32),
            ),
          ],
        ),
      ),
    );
  }
}

class _PodiumSlot extends StatelessWidget {
  final int position;
  final String name;
  final String? avatarUrl;
  final int points;
  final double barHeight;
  final Color medalColor;

  const _PodiumSlot({
    required this.position,
    required this.name,
    this.avatarUrl,
    required this.points,
    required this.barHeight,
    required this.medalColor,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      mainAxisSize: MainAxisSize.min,
      children: [
        DayPilotAvatar(name: name, imageUrl: avatarUrl, size: 52),
        const SizedBox(height: 4),
        Text(
          name.split(' ').first,
          style: text.labelMedium?.copyWith(fontWeight: FontWeight.w600),
        ),
        Text(
          '$points pts',
          style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
        ),
        const SizedBox(height: 8),
        Container(
          width: 76,
          height: barHeight,
          decoration: BoxDecoration(
            color: medalColor.withAlpha(50),
            borderRadius: const BorderRadius.vertical(top: Radius.circular(8)),
            border: Border.all(color: medalColor.withAlpha(140)),
          ),
          alignment: Alignment.center,
          child: Text(
            '$position',
            style: text.headlineMedium?.copyWith(
              color: medalColor,
              fontWeight: FontWeight.w800,
            ),
          ),
        ),
      ],
    );
  }
}
