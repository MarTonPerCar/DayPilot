import 'package:flutter/material.dart';

/// Píldora de ancho completo con dos segmentos (puntos / racha)
/// separados por un divisor vertical.
class StatsPill extends StatelessWidget {
  final int points;
  final int streak;

  const StatsPill({super.key, required this.points, required this.streak});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;

    return Container(
      padding: const EdgeInsets.symmetric(vertical: 10),
      decoration: BoxDecoration(
        color: colors.surfaceContainerHigh,
        borderRadius: BorderRadius.circular(16),
      ),
      child: Row(
        children: [
          Expanded(child: _Segment(label: 'pts', value: '$points')),
          SizedBox(
            height: 28,
            child: VerticalDivider(width: 1, color: colors.outlineVariant),
          ),
          Expanded(child: _Segment(label: '🔥 racha', value: '$streak')),
        ],
      ),
    );
  }
}

class _Segment extends StatelessWidget {
  final String label;
  final String value;

  const _Segment({required this.label, required this.value});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      children: [
        Text(label, style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
        const SizedBox(height: 2),
        Text(value, style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700)),
      ],
    );
  }
}
