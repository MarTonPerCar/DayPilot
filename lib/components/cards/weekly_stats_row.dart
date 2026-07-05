import 'package:flutter/material.dart';

/// Compartida entre FriendCard y WeeklyReactionCard para que se vean igual.
class WeeklyStatsRow extends StatelessWidget {
  final int points;
  final int tasks;
  final int steps;
  final int streak;

  const WeeklyStatsRow({
    super.key,
    required this.points,
    required this.tasks,
    required this.steps,
    required this.streak,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(child: _Stat(emoji: '⭐', value: '$points', label: 'pts esta semana')),
        Expanded(child: _Stat(emoji: '✅', value: '$tasks', label: 'tareas')),
        Expanded(child: _Stat(emoji: '👣', value: _fmt(steps), label: 'pasos')),
        Expanded(child: _Stat(emoji: '🔥', value: '${streak}d', label: 'mejor racha')),
      ],
    );
  }

  static String _fmt(int n) => n >= 1000 ? '${(n / 1000).toStringAsFixed(1)}k' : '$n';
}

class _Stat extends StatelessWidget {
  final String emoji;
  final String value;
  final String label;

  const _Stat({required this.emoji, required this.value, required this.label});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      children: [
        Text(emoji, style: const TextStyle(fontSize: 16)),
        const SizedBox(height: 2),
        Text(value, style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
        Text(
          label,
          textAlign: TextAlign.center,
          style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
        ),
      ],
    );
  }
}
