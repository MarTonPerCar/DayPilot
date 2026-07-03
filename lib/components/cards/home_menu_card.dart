import 'dart:math' as math;
import 'package:flutter/material.dart';

class HomeMenuCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final Color accentColor;
  final Widget indicator;
  final VoidCallback? onTap;

  const HomeMenuCard({
    super.key,
    required this.icon,
    required this.title,
    required this.accentColor,
    required this.indicator,
    this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card(
      elevation: 2,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.all(Radius.circular(20)),
      ),
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Stack(
          fit: StackFit.expand,
          children: [
            // Gradient background overlay
            Container(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                  colors: [
                    accentColor.withAlpha(31),
                    accentColor.withAlpha(8),
                  ],
                ),
              ),
            ),

            // Large decorative icon — bottom-right, rotated -15°, faint
            Positioned(
              bottom: -10,
              right: -10,
              child: Transform.rotate(
                angle: -15 * math.pi / 180,
                child: Icon(icon, size: 84, color: accentColor.withAlpha(20)),
              ),
            ),

            // Foreground content
            Padding(
              padding: const EdgeInsets.all(14),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  // Small icon in tinted pill
                  Container(
                    width: 36,
                    height: 36,
                    decoration: BoxDecoration(
                      color: accentColor.withAlpha(45),
                      borderRadius: BorderRadius.circular(10),
                    ),
                    child: Icon(icon, size: 20, color: accentColor),
                  ),
                  const SizedBox(height: 8),

                  Text(
                    title,
                    style: text.titleSmall?.copyWith(
                      fontWeight: FontWeight.w700,
                      color: colors.onSurface,
                    ),
                  ),

                  const Spacer(),

                  // Section-specific data indicator
                  indicator,
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ── Pre-built indicator widgets ────────────────────────────────────────────────

class HomeMenuProgressBar extends StatelessWidget {
  final double value;
  final String label;
  final Color color;

  const HomeMenuProgressBar({
    super.key,
    required this.value,
    required this.label,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Text(
          label,
          style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
        ),
        const SizedBox(height: 4),
        LinearProgressIndicator(
          value: value.clamp(0.0, 1.0),
          minHeight: 6,
          borderRadius: BorderRadius.circular(3),
          color: color,
          backgroundColor: color.withAlpha(30),
        ),
      ],
    );
  }
}

class HomeMenuMiniBarChart extends StatelessWidget {
  final List<double> values;
  final Color color;

  const HomeMenuMiniBarChart({
    super.key,
    required this.values,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final max = values.reduce((a, b) => a > b ? a : b);
    return Row(
      crossAxisAlignment: CrossAxisAlignment.end,
      children: values
          .map(
            (v) => Expanded(
              child: Container(
                margin: const EdgeInsets.symmetric(horizontal: 1),
                height: 20 * (max > 0 ? v / max : 0),
                decoration: BoxDecoration(
                  color: color.withAlpha(160),
                  borderRadius: BorderRadius.circular(2),
                ),
              ),
            ),
          )
          .toList(),
    );
  }
}

class HomeMenuHabitsIndicator extends StatelessWidget {
  final double stepsProgress;
  final String stepsLabel;
  final String timerLabel;
  final Color color;

  const HomeMenuHabitsIndicator({
    super.key,
    required this.stepsProgress,
    required this.stepsLabel,
    required this.timerLabel,
    required this.color,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Row(
          children: [
            Icon(Icons.directions_walk_rounded, size: 13, color: color),
            const SizedBox(width: 4),
            Expanded(
              child: Text(
                stepsLabel,
                style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
        const SizedBox(height: 3),
        ClipRRect(
          borderRadius: BorderRadius.circular(2),
          child: LinearProgressIndicator(
            value: stepsProgress.clamp(0.0, 1.0),
            minHeight: 4,
            color: color,
            backgroundColor: color.withAlpha(30),
          ),
        ),
        const SizedBox(height: 4),
        Row(
          children: [
            Icon(Icons.timer_outlined, size: 13, color: color),
            const SizedBox(width: 4),
            Expanded(
              child: Text(
                timerLabel,
                style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
          ],
        ),
      ],
    );
  }
}

class HomeMenuRivalryIndicator extends StatelessWidget {
  final int position;
  final int total;
  final Color color;

  const HomeMenuRivalryIndicator({
    super.key,
    required this.position,
    required this.total,
    required this.color,
  });

  /// 1º/2º/3º si estás en el podio; si no, "···+posición".
  String get _label => switch (position) {
        1 => '1º',
        2 => '2º',
        3 => '3º',
        _ => '···+$position',
      };

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisSize: MainAxisSize.min,
      children: [
        Row(
          children: [
            Icon(Icons.emoji_events_rounded, size: 16, color: color),
            const SizedBox(width: 4),
            Text(
              _label,
              style: text.labelSmall?.copyWith(
                color: colors.onSurfaceVariant,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
        const SizedBox(height: 6),
        Row(
          children: List.generate(total, (i) {
            final active = i == position - 1;
            return Expanded(
              child: Container(
                margin: EdgeInsets.only(right: i < total - 1 ? 4 : 0),
                height: 5,
                decoration: BoxDecoration(
                  color: active ? color : color.withAlpha(40),
                  borderRadius: BorderRadius.circular(3),
                ),
              ),
            );
          }),
        ),
      ],
    );
  }
}
