import 'dart:math';
import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

/// Tarjeta principal de "Pasos" del hub de hábitos: medidor circular de
/// progreso, hitos (50/75/100%), y resumen de puntos ganados hoy.
class StepsProgressCard extends StatelessWidget {
  final int steps;
  final int goal;
  final int pointsEarnedToday;
  final VoidCallback onConfigureGoal;

  const StepsProgressCard({
    super.key,
    required this.steps,
    required this.goal,
    required this.pointsEarnedToday,
    required this.onConfigureGoal,
  });

  static const _milestones = [50, 75, 100];

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final ratio = goal > 0 ? (steps / goal).clamp(0.0, 1.0) : 0.0;
    final percent = (ratio * 100).round();
    final nextMilestone = _milestones.firstWhere((m) => percent < m, orElse: () => 100);

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
                Icon(Icons.directions_walk_rounded, color: colors.primary, size: 22),
                const SizedBox(width: 8),
                Text(l10n.commonSteps, style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
                const Spacer(),
                GestureDetector(
                  onTap: onConfigureGoal,
                  child: Text(
                    l10n.stepsConfigureGoal,
                    style: text.labelLarge?.copyWith(
                      color: colors.primary,
                      fontWeight: FontWeight.w600,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),
            Row(
              crossAxisAlignment: CrossAxisAlignment.center,
              children: [
                SizedBox(
                  width: 140,
                  height: 140,
                  child: CustomPaint(
                    painter: _GaugePainter(
                      progress: ratio,
                      trackColor: colors.surfaceContainerHighest,
                      progressColor: colors.primary,
                    ),
                    child: Center(
                      child: Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            '$steps',
                            style: text.headlineSmall?.copyWith(fontWeight: FontWeight.w700),
                          ),
                          Text(
                            l10n.stepsOfGoal(goal),
                            style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                          ),
                          Text(
                            '$percent%',
                            style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 20),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        l10n.stepsMilestones,
                        style: text.labelLarge?.copyWith(color: colors.onSurfaceVariant),
                      ),
                      const SizedBox(height: 8),
                      Wrap(
                        spacing: 8,
                        runSpacing: 8,
                        children: _milestones.map((m) {
                          final reached = percent >= m;
                          return Container(
                            padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
                            decoration: BoxDecoration(
                              color: reached ? colors.primaryContainer : colors.surfaceContainerHighest,
                              borderRadius: BorderRadius.circular(20),
                            ),
                            child: Text(
                              '$m%',
                              style: text.labelMedium?.copyWith(
                                color: reached ? colors.onPrimaryContainer : colors.onSurfaceVariant,
                                fontWeight: FontWeight.w600,
                              ),
                            ),
                          );
                        }).toList(),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            Divider(height: 1, color: colors.outlineVariant),
            const SizedBox(height: 12),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(l10n.stepsPointsEarnedToday, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
                Text(l10n.commonPointsSuffix(pointsEarnedToday), style: text.bodyMedium?.copyWith(fontWeight: FontWeight.w700)),
              ],
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(l10n.stepsNextGoal, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
                Text('$nextMilestone%', style: text.bodyMedium?.copyWith(fontWeight: FontWeight.w700)),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _GaugePainter extends CustomPainter {
  final double progress;
  final Color trackColor;
  final Color progressColor;

  _GaugePainter({
    required this.progress,
    required this.trackColor,
    required this.progressColor,
  });

  static const _startAngle = 3 * pi / 4; // 135°
  static const _sweepAngle = 3 * pi / 2; // 270°

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = (min(size.width, size.height) - 14) / 2;
    const strokeWidth = 10.0;

    final trackPaint = Paint()
      ..color = trackColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round;
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      _startAngle,
      _sweepAngle,
      false,
      trackPaint,
    );

    final progressPaint = Paint()
      ..color = progressColor
      ..style = PaintingStyle.stroke
      ..strokeWidth = strokeWidth
      ..strokeCap = StrokeCap.round;
    final sweep = _sweepAngle * progress;
    canvas.drawArc(
      Rect.fromCircle(center: center, radius: radius),
      _startAngle,
      sweep,
      false,
      progressPaint,
    );

    final dotAngle = _startAngle + sweep;
    final dotCenter = Offset(
      center.dx + radius * cos(dotAngle),
      center.dy + radius * sin(dotAngle),
    );
    canvas.drawCircle(dotCenter, strokeWidth / 2 + 1, Paint()..color = progressColor);
  }

  @override
  bool shouldRepaint(covariant _GaugePainter oldDelegate) {
    return oldDelegate.progress != progress || oldDelegate.progressColor != progressColor;
  }
}
