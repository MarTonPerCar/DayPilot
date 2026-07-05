import 'package:flutter/material.dart';
import '../basic/task_dot.dart';

class DifficultyField extends StatelessWidget {
  final TaskDifficulty value;
  final ValueChanged<TaskDifficulty> onChanged;
  final String? label;

  const DifficultyField({
    super.key,
    required this.value,
    required this.onChanged,
    this.label,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (label != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 8),
            child: Text(label!, style: text.bodyMedium?.copyWith(color: colors.onSurface)),
          ),
        Row(
          children: TaskDifficulty.values.map((d) {
            final selected = d == value;
            final color = d.color(colors);
            return Expanded(
              child: Padding(
                padding: const EdgeInsets.symmetric(horizontal: 3),
                child: GestureDetector(
                  onTap: () => onChanged(d),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 150),
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    decoration: BoxDecoration(
                      color: selected ? color : color.withAlpha(30),
                      borderRadius: BorderRadius.circular(10),
                      border: Border.all(color: color, width: selected ? 0 : 1.4),
                    ),
                    alignment: Alignment.center,
                    child: Text(
                      d.label(context),
                      style: text.labelLarge?.copyWith(
                        fontWeight: FontWeight.w700,
                        color: selected ? Colors.white : color,
                      ),
                    ),
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }
}
