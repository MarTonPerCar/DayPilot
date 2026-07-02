import 'package:flutter/material.dart';

class DayPilotRadioGroup<T> extends StatelessWidget {
  final String? label;
  final T value;
  final List<T> options;
  final String Function(T) display;
  final ValueChanged<T> onChanged;

  const DayPilotRadioGroup({
    super.key,
    this.label,
    required this.value,
    required this.options,
    required this.display,
    required this.onChanged,
  });

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final colors = Theme.of(context).colorScheme;

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        if (label != null)
          Padding(
            padding: const EdgeInsets.only(bottom: 4, left: 4),
            child: Text(
              label!,
              style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
            ),
          ),
        ...options.map((opt) {
          return RadioListTile<T>(
            title: Text(display(opt)),
            value: opt,
            groupValue: value,
            onChanged: (v) { if (v != null) onChanged(v); },
            contentPadding: const EdgeInsets.symmetric(horizontal: 4),
            dense: true,
          );
        }),
      ],
    );
  }
}
