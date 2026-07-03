import 'package:flutter/material.dart';
import '../basic/task_category.dart';
import '../basic/task_dot.dart';

/// Fila de tarea del calendario: checkbox, título, chips de categoría y
/// duración, y accesos directos de editar/eliminar. La franja superior se
/// divide en dos: la mitad izquierda es el color de dificultad, la derecha
/// el color de categoría.
class CalendarTaskCard extends StatelessWidget {
  final String title;
  final TaskDifficulty difficulty;
  final TaskCategory category;
  final int durationMinutes;
  final bool completed;
  final VoidCallback? onToggle;
  final VoidCallback? onTap;
  final VoidCallback? onEdit;
  final VoidCallback? onDelete;

  const CalendarTaskCard({
    super.key,
    required this.title,
    required this.difficulty,
    required this.category,
    required this.durationMinutes,
    this.completed = false,
    this.onToggle,
    this.onTap,
    this.onEdit,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                Expanded(child: Container(height: 4, color: difficulty.color(colors))),
                Expanded(child: Container(height: 4, color: category.color)),
              ],
            ),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 4, vertical: 6),
              child: Row(
                children: [
                  Checkbox(
                    value: completed,
                    onChanged: (_) => onToggle?.call(),
                    shape: const CircleBorder(),
                  ),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          title,
                          style: text.bodyLarge?.copyWith(
                            decoration: completed ? TextDecoration.lineThrough : null,
                            color: completed ? colors.onSurfaceVariant : colors.onSurface,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Wrap(
                          spacing: 8,
                          runSpacing: 4,
                          children: [
                            TaskCategoryChip(category: category),
                            DurationChip(minutes: durationMinutes),
                          ],
                        ),
                      ],
                    ),
                  ),
                  IconButton(
                    icon: const Icon(Icons.edit_outlined, size: 20),
                    onPressed: onEdit,
                    visualDensity: VisualDensity.compact,
                  ),
                  IconButton(
                    icon: Icon(Icons.delete_outline_rounded, size: 20, color: colors.error),
                    onPressed: onDelete,
                    visualDensity: VisualDensity.compact,
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class DurationChip extends StatelessWidget {
  final int minutes;
  const DurationChip({super.key, required this.minutes});

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: colors.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(Icons.timer_outlined, size: 12, color: colors.onSurfaceVariant),
          const SizedBox(width: 4),
          Text(
            '${minutes}min',
            style: text.labelSmall?.copyWith(
              color: colors.onSurfaceVariant,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}
