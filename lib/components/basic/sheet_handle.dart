import 'package:flutter/material.dart';

/// Tirador de arrastre para hojas inferiores (bottom sheets). Centralizado
/// para que el gesto visual sea idéntico en todas las hojas de la app.
class DayPilotSheetHandle extends StatelessWidget {
  const DayPilotSheetHandle({super.key});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Center(
      child: Container(
        width: 36,
        height: 4,
        decoration: BoxDecoration(
          color: colors.outlineVariant,
          borderRadius: BorderRadius.circular(2),
        ),
      ),
    );
  }
}
