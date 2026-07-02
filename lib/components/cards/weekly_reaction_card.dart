import 'package:flutter/material.dart';

class WeeklyReactionCard extends StatelessWidget {
  final String weekLabel;
  final int points;
  final int steps;
  final int tasks;
  final Map<String, int> reactions;

  const WeeklyReactionCard({
    super.key,
    required this.weekLabel,
    required this.points,
    required this.steps,
    required this.tasks,
    this.reactions = const {},
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

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
            Row(
              children: [
                _WeekStat(
                  icon: Icons.star_rounded,
                  color: const Color(0xFFFFD700),
                  value: '$points',
                  label: 'puntos',
                ),
                _WeekStat(
                  icon: Icons.directions_walk_rounded,
                  color: colors.tertiary,
                  value: _formatSteps(steps),
                  label: 'pasos',
                ),
                _WeekStat(
                  icon: Icons.task_alt_rounded,
                  color: colors.primary,
                  value: '$tasks',
                  label: 'tareas',
                ),
              ],
            ),
            if (reactions.isNotEmpty) ...[
              const SizedBox(height: 16),
              Text(
                'Reacciones recibidas',
                style: text.labelMedium
                    ?.copyWith(color: colors.onSurfaceVariant),
              ),
              const SizedBox(height: 8),
              Wrap(
                spacing: 8,
                runSpacing: 6,
                children: reactions.entries.map((e) {
                  return Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: colors.surfaceContainerHighest,
                      borderRadius: BorderRadius.circular(20),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Text(e.key, style: const TextStyle(fontSize: 18)),
                        const SizedBox(width: 4),
                        Text(
                          '${e.value}',
                          style: text.labelMedium?.copyWith(
                            fontWeight: FontWeight.w600,
                            color: colors.onSurface,
                          ),
                        ),
                      ],
                    ),
                  );
                }).toList(),
              ),
            ],
          ],
        ),
      ),
    );
  }

  String _formatSteps(int s) {
    if (s >= 1000) return '${(s / 1000).toStringAsFixed(1)}k';
    return '$s';
  }
}

class _WeekStat extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String value;
  final String label;

  const _WeekStat({
    required this.icon,
    required this.color,
    required this.value,
    required this.label,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;

    return Expanded(
      child: Row(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 6),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(value,
                  style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
              Text(label,
                  style: text.labelSmall
                      ?.copyWith(color: colors.onSurfaceVariant)),
            ],
          ),
        ],
      ),
    );
  }
}
