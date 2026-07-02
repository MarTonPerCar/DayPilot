import 'package:flutter/material.dart';

class DailySummaryCard extends StatelessWidget {
  final int pointsFromTasks;
  final int pointsFromSteps;
  final int pointsFromTimer;
  final int pointsFromHealth;
  final int pointsFromWellness;

  const DailySummaryCard({
    super.key,
    this.pointsFromTasks = 0,
    this.pointsFromSteps = 0,
    this.pointsFromTimer = 0,
    this.pointsFromHealth = 0,
    this.pointsFromWellness = 0,
  });

  int get _total =>
      pointsFromTasks +
      pointsFromSteps +
      pointsFromTimer +
      pointsFromHealth +
      pointsFromWellness;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final maxVal = [
      pointsFromTasks,
      pointsFromSteps,
      pointsFromTimer,
      pointsFromHealth,
      pointsFromWellness,
    ].reduce((a, b) => a > b ? a : b).clamp(1, 1 << 30);

    final sources = [
      _Source('Tareas', Icons.task_alt_rounded, pointsFromTasks, colors.primary),
      _Source('Pasos', Icons.directions_walk_rounded, pointsFromSteps, colors.tertiary),
      _Source('Temporizador', Icons.timer_rounded, pointsFromTimer, colors.secondary),
      _Source('Salud tech', Icons.health_and_safety_rounded, pointsFromHealth, colors.error),
      _Source('Bienestar', Icons.self_improvement_rounded, pointsFromWellness, const Color(0xFF9C6FDE)),
    ];

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Resumen del día',
                  style: text.titleMedium?.copyWith(fontWeight: FontWeight.w600),
                ),
                Row(
                  children: [
                    Icon(Icons.star_rounded,
                        size: 18, color: const Color(0xFFFFD700)),
                    const SizedBox(width: 4),
                    Text(
                      '$_total pts',
                      style: text.titleSmall?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: colors.onSurface,
                      ),
                    ),
                  ],
                ),
              ],
            ),
            const SizedBox(height: 16),
            ...sources.map((s) => _SourceRow(
                  source: s,
                  maxVal: maxVal,
                )),
          ],
        ),
      ),
    );
  }
}

class _Source {
  final String label;
  final IconData icon;
  final int points;
  final Color color;
  const _Source(this.label, this.icon, this.points, this.color);
}

class _SourceRow extends StatelessWidget {
  final _Source source;
  final int maxVal;

  const _SourceRow({required this.source, required this.maxVal});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final ratio = (source.points / maxVal).clamp(0.0, 1.0);

    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Row(
        children: [
          Icon(source.icon, size: 18, color: source.color),
          const SizedBox(width: 10),
          SizedBox(
            width: 90,
            child: Text(
              source.label,
              style: text.bodySmall?.copyWith(color: colors.onSurface),
            ),
          ),
          Expanded(
            child: ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: ratio,
                minHeight: 8,
                backgroundColor: source.color.withAlpha(25),
                color: source.color,
              ),
            ),
          ),
          const SizedBox(width: 10),
          SizedBox(
            width: 44,
            child: Text(
              '+${source.points}',
              style: text.labelMedium?.copyWith(
                fontWeight: FontWeight.w600,
                color: colors.onSurface,
              ),
              textAlign: TextAlign.right,
            ),
          ),
        ],
      ),
    );
  }
}
