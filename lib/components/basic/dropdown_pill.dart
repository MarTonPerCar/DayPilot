import 'package:flutter/material.dart';

class DropdownPillItem<T> {
  /// `null` representa la opción "Todas" (sin filtro).
  final T? value;
  final String label;
  final IconData icon;
  final Color? color;

  const DropdownPillItem({
    required this.value,
    required this.label,
    required this.icon,
    this.color,
  });
}

class DropdownPill<T> extends StatelessWidget {
  final String label;
  final List<DropdownPillItem<T>> items;
  final T? selected;
  final ValueChanged<T?> onChanged;

  const DropdownPill({
    super.key,
    required this.label,
    required this.items,
    required this.selected,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;

    return PopupMenuButton<T?>(
      onSelected: onChanged,
      offset: const Offset(0, 8),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      itemBuilder: (context) => items.map((item) {
        final isSelected = item.value == selected;
        final color = item.color ?? colors.onSurface;
        return PopupMenuItem<T?>(
          value: item.value,
          child: Row(
            children: [
              Icon(item.icon, size: 18, color: color),
              const SizedBox(width: 12),
              Text(
                item.label,
                style: text.bodyMedium?.copyWith(
                  color: color,
                  fontWeight: isSelected ? FontWeight.w700 : FontWeight.w400,
                ),
              ),
            ],
          ),
        );
      }).toList(),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        decoration: BoxDecoration(
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: colors.outline),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(label, style: text.bodyMedium?.copyWith(color: colors.onSurface)),
            const SizedBox(width: 6),
            Icon(Icons.keyboard_arrow_down_rounded, size: 18, color: colors.onSurfaceVariant),
          ],
        ),
      ),
    );
  }
}
