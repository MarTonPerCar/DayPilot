import 'package:flutter/material.dart';

class DayPilotReactions extends StatelessWidget {
  final String? selected;
  final void Function(String) onReact;

  static const _emojis = ['👍', '❤️', '🔥', '⭐', '😮', '😢'];

  const DayPilotReactions({
    super.key,
    this.selected,
    required this.onReact,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return Row(
      mainAxisSize: MainAxisSize.min,
      children: _emojis.map((emoji) {
        final isSelected = emoji == selected;
        return GestureDetector(
          onTap: () => onReact(emoji),
          child: AnimatedContainer(
            duration: const Duration(milliseconds: 150),
            margin: const EdgeInsets.only(right: 8),
            padding: const EdgeInsets.all(8),
            decoration: BoxDecoration(
              color: isSelected
                  ? colors.primaryContainer
                  : colors.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(20),
            ),
            child: Text(emoji, style: const TextStyle(fontSize: 20)),
          ),
        );
      }).toList(),
    );
  }
}
