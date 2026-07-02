import 'package:flutter/material.dart';

class StepsCard extends StatelessWidget {
  final int steps;
  final int goal;
  final int pointsEarned;

  const StepsCard({
    super.key,
    required this.steps,
    this.goal = 10000,
    this.pointsEarned = 0,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final ratio = (steps / goal).clamp(0.0, 1.0);

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Icon(Icons.directions_walk_rounded,
                    size: 28, color: colors.tertiary),
                const SizedBox(width: 10),
                Text(
                  _formatNum(steps),
                  style: text.displaySmall?.copyWith(
                    fontWeight: FontWeight.w700,
                    color: colors.onSurface,
                  ),
                ),
                const SizedBox(width: 4),
                Padding(
                  padding: const EdgeInsets.only(bottom: 4),
                  child: Text(
                    'pasos',
                    style: text.titleMedium
                        ?.copyWith(color: colors.onSurfaceVariant),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              'Meta: ${_formatNum(goal)} pasos',
              style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
            ),
            const SizedBox(height: 14),
            ClipRRect(
              borderRadius: BorderRadius.circular(6),
              child: LinearProgressIndicator(
                value: ratio,
                minHeight: 10,
                backgroundColor: colors.tertiaryContainer,
                color: ratio >= 1.0 ? colors.primary : colors.tertiary,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  '${(ratio * 100).toStringAsFixed(0)}% completado',
                  style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                ),
                if (pointsEarned > 0)
                  Row(
                    children: [
                      Icon(Icons.star_rounded,
                          size: 14, color: const Color(0xFFFFD700)),
                      const SizedBox(width: 3),
                      Text(
                        '+$pointsEarned pts',
                        style: text.labelSmall?.copyWith(
                          color: colors.onSurface,
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _formatNum(int n) {
    if (n >= 1000) {
      final s = n.toString();
      final left = s.substring(0, s.length - 3);
      final right = s.substring(s.length - 3);
      return '$left.$right';
    }
    return '$n';
  }
}

class StepsSummaryCard extends StatelessWidget {
  final List<int> weeklySteps;
  final int goal;

  const StepsSummaryCard({
    super.key,
    required this.weeklySteps,
    this.goal = 10000,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final labels = ['L', 'M', 'X', 'J', 'V', 'S', 'D'];
    final maxVal = weeklySteps.reduce((a, b) => a > b ? a : b).clamp(1, 1 << 30);
    final total = weeklySteps.reduce((a, b) => a + b);
    final avg = total ~/ 7;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Esta semana',
                  style: text.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                ),
                Text(
                  'Media: ${_fmt(avg)}/día',
                  style:
                      text.labelMedium?.copyWith(color: colors.onSurfaceVariant),
                ),
              ],
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 100,
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: List.generate(7, (i) {
                  final s = weeklySteps[i];
                  final barH = (s / maxVal * 80).clamp(4.0, 80.0);
                  final reached = s >= goal;
                  return Expanded(
                    child: Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 3),
                      child: Column(
                        mainAxisAlignment: MainAxisAlignment.end,
                        children: [
                          Container(
                            height: barH,
                            decoration: BoxDecoration(
                              color: reached ? colors.primary : colors.primaryContainer,
                              borderRadius: BorderRadius.circular(5),
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(
                            labels[i],
                            style: text.labelSmall
                                ?.copyWith(color: colors.onSurfaceVariant),
                          ),
                        ],
                      ),
                    ),
                  );
                }),
              ),
            ),
            const SizedBox(height: 10),
            Row(
              children: [
                Icon(Icons.directions_walk_rounded,
                    size: 16, color: colors.tertiary),
                const SizedBox(width: 6),
                Text(
                  'Total: ${_fmt(total)} pasos',
                  style: text.labelMedium?.copyWith(fontWeight: FontWeight.w600),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  String _fmt(int n) {
    if (n >= 1000) {
      final s = n.toString();
      return '${s.substring(0, s.length - 3)}.${s.substring(s.length - 3)}';
    }
    return '$n';
  }
}
