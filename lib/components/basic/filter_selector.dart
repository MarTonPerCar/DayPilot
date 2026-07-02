import 'package:flutter/material.dart';

class DayPilotFilterSelector<T> extends StatelessWidget {
  final List<T> options;
  final T selected;
  final String Function(T) label;
  final void Function(T) onSelected;

  const DayPilotFilterSelector({
    super.key,
    required this.options,
    required this.selected,
    required this.label,
    required this.onSelected,
  });

  @override
  Widget build(BuildContext context) {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      child: Row(
        children: options.map((option) {
          return Padding(
            padding: const EdgeInsets.only(right: 8),
            child: FilterChip(
              label: Text(label(option)),
              selected: option == selected,
              onSelected: (_) => onSelected(option),
            ),
          );
        }).toList(),
      ),
    );
  }
}
