import 'package:flutter/material.dart';
import '../../data/app_data.dart';

/// Fila de preset de cronómetro: icono, nombre, descripción y botón de
/// inicio rápido coloreado según el preset.
class TimerPresetCard extends StatelessWidget {
  final TimerPreset preset;
  final VoidCallback onPlay;

  const TimerPresetCard({super.key, required this.preset, required this.onPlay});

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        child: Row(
          children: [
            Container(
              width: 52,
              height: 52,
              decoration: BoxDecoration(
                color: preset.color.withAlpha(40),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(preset.icon, color: preset.color, size: 26),
            ),
            const SizedBox(width: 14),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(preset.name, style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
                  Text(
                    preset.description,
                    style: text.bodySmall?.copyWith(color: Theme.of(context).colorScheme.onSurfaceVariant),
                  ),
                ],
              ),
            ),
            Material(
              color: preset.color,
              borderRadius: BorderRadius.circular(14),
              child: InkWell(
                onTap: onPlay,
                borderRadius: BorderRadius.circular(14),
                child: const Padding(
                  padding: EdgeInsets.all(12),
                  child: Icon(Icons.play_arrow_rounded, color: Colors.white, size: 22),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
