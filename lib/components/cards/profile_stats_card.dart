import 'package:flutter/material.dart';

class ProfileStatsCard extends StatelessWidget {
  final int level;
  final int totalPoints;
  final int streak;
  final int tasksCompleted;

  const ProfileStatsCard({
    super.key,
    required this.level,
    required this.totalPoints,
    required this.streak,
    required this.tasksCompleted,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            Row(
              children: [
                Container(
                  width: 64,
                  height: 64,
                  decoration: BoxDecoration(
                    color: colors.primaryContainer,
                    shape: BoxShape.circle,
                    border: Border.all(color: colors.primary, width: 2),
                  ),
                  child: Center(
                    child: Text(
                      '$level',
                      style: text.headlineSmall?.copyWith(
                        color: colors.onPrimaryContainer,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Nivel $level',
                      style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700),
                    ),
                    Text(
                      '$totalPoints puntos totales',
                      style: text.bodyMedium
                          ?.copyWith(color: colors.onSurfaceVariant),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              children: [
                _StatTile(
                  icon: Icons.local_fire_department_rounded,
                  color: colors.error,
                  label: 'Racha',
                  value: '$streak días',
                ),
                const SizedBox(width: 12),
                _StatTile(
                  icon: Icons.task_alt_rounded,
                  color: colors.primary,
                  label: 'Tareas',
                  value: '$tasksCompleted',
                ),
                const SizedBox(width: 12),
                _StatTile(
                  icon: Icons.star_rounded,
                  color: const Color(0xFFFFD700),
                  label: 'Puntos',
                  value: _formatPoints(totalPoints),
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

class _StatTile extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String label;
  final String value;

  const _StatTile({
    required this.icon,
    required this.color,
    required this.label,
    required this.value,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Expanded(
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: colors.surfaceContainerHighest,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          children: [
            Icon(icon, color: color, size: 22),
            const SizedBox(height: 4),
            Text(
              value,
              style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700),
            ),
            Text(
              label,
              style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}
