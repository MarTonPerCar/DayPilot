import 'package:flutter/material.dart';
import '../basic/task_category.dart';

class CategoryChipGroup extends StatelessWidget {
  final TaskCategory? selected;
  final ValueChanged<TaskCategory> onChanged;
  final String? label;

  const CategoryChipGroup({
    super.key,
    required this.selected,
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
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: TaskCategory.values.map((c) {
            final isSelected = c == selected;
            return GestureDetector(
              onTap: () => onChanged(c),
              child: AnimatedContainer(
                duration: const Duration(milliseconds: 150),
                padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                decoration: BoxDecoration(
                  color: isSelected ? colors.primary : Colors.transparent,
                  borderRadius: BorderRadius.circular(10),
                  border: Border.all(color: isSelected ? colors.primary : colors.outlineVariant),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Icon(c.icon, size: 16, color: isSelected ? colors.onPrimary : c.color),
                    const SizedBox(width: 6),
                    Text(
                      c.label(context),
                      style: text.bodyMedium?.copyWith(
                        color: isSelected ? colors.onPrimary : colors.onSurface,
                        fontWeight: isSelected ? FontWeight.w700 : FontWeight.w400,
                      ),
                    ),
                  ],
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }
}
