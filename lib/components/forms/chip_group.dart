import 'package:flutter/material.dart';

class DayPilotChipGroup<T> extends StatelessWidget {
  final String? label;
  final List<T> options;
  final List<T> selected;
  final String Function(T) display;
  final ValueChanged<List<T>> onChanged;
  final bool singleSelect;

  const DayPilotChipGroup({
    super.key,
    this.label,
    required this.options,
    required this.selected,
    required this.display,
    required this.onChanged,
    this.singleSelect = false,
  });

  void _toggle(T option) {
    if (singleSelect) {
      onChanged([option]);
      return;
    }
    final next = List<T>.from(selected);
    if (next.contains(option)) {
      next.remove(option);
    } else {
      next.add(option);
    }
    onChanged(next);
  }

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (label != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Text(
                label!,
                style: text.bodyMedium?.copyWith(color: colors.onSurface),
              ),
            ),
          Wrap(
            spacing: 8,
            runSpacing: 6,
            children: options.map((opt) {
              final isSelected = selected.contains(opt);
              return FilterChip(
                label: Text(display(opt)),
                selected: isSelected,
                onSelected: (_) => _toggle(opt),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }
}
