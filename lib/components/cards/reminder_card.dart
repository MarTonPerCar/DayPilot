import 'package:flutter/material.dart';

/// Fila de recordatorio: título, fecha/hora relativa, eliminar y activar.
class ReminderCard extends StatelessWidget {
  final String title;
  final DateTime dateTime;
  final bool enabled;
  final ValueChanged<bool> onToggle;
  final VoidCallback onDelete;

  const ReminderCard({
    super.key,
    required this.title,
    required this.dateTime,
    required this.enabled,
    required this.onToggle,
    required this.onDelete,
  });

  static String formatWhen(DateTime dt, DateTime now) {
    final time = '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
    final today = DateTime(now.year, now.month, now.day);
    final target = DateTime(dt.year, dt.month, dt.day);
    final diff = target.difference(today).inDays;
    if (diff == 0) return 'Hoy, $time';
    if (diff == 1) return 'Mañana, $time';
    const months = [
      'ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic',
    ];
    return '${dt.day} ${months[dt.month - 1]}, $time';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w700)),
                  Text(
                    formatWhen(dateTime, DateTime.now()),
                    style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                  ),
                ],
              ),
            ),
            IconButton(
              icon: Icon(Icons.delete_outline_rounded, color: colors.error),
              onPressed: onDelete,
            ),
            Switch(value: enabled, onChanged: onToggle),
          ],
        ),
      ),
    );
  }
}
