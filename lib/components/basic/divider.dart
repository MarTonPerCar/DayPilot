import 'package:flutter/material.dart';

class DayPilotDivider extends StatelessWidget {
  final String? label;

  const DayPilotDivider({super.key, this.label});

  @override
  Widget build(BuildContext context) {
    if (label == null) return const Divider();

    return Row(
      children: [
        const Expanded(child: Divider()),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12),
          child: Text(
            label!,
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                  color: Theme.of(context).colorScheme.onSurfaceVariant,
                ),
          ),
        ),
        const Expanded(child: Divider()),
      ],
    );
  }
}
