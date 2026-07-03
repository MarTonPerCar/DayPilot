import 'package:flutter/material.dart';

/// Casilla de estadística (icono + valor + etiqueta) usada en las tarjetas
/// de resumen de perfil y de puntos del día. Se coloca directamente dentro
/// de un [Row]: ya incluye su propio [Expanded].
class StatTile extends StatelessWidget {
  final IconData icon;
  final Color color;
  final String value;
  final String label;

  const StatTile({
    super.key,
    required this.icon,
    required this.color,
    required this.value,
    required this.label,
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
            Text(value, style: text.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
            Text(
              label,
              textAlign: TextAlign.center,
              style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
            ),
          ],
        ),
      ),
    );
  }
}
