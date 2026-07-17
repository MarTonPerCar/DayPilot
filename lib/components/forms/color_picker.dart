import 'package:flutter/material.dart';

class DayPilotColorPicker extends StatelessWidget {
  final Color value;
  final List<Color> colors;
  final ValueChanged<Color> onChanged;
  final String? label;
  final double dotSize;

  const DayPilotColorPicker({
    super.key,
    required this.value,
    required this.colors,
    required this.onChanged,
    this.label,
    this.dotSize = 36,
  });

  static const defaultColors = [
    Color(0xFF4C9966),
    Color(0xFF0055A4),
    Color(0xFF7B5EA7),
    Color(0xFFE8A020),
    Color(0xFFE53935),
    Color(0xFF00897B),
    Color(0xFFFF7043),
    Color(0xFF5E35B1),
    Color(0xFF757575),
    Color(0xFF212121),
  ];

  @override
  Widget build(BuildContext context) {
    final text = Theme.of(context).textTheme;
    final scheme = Theme.of(context).colorScheme;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (label != null)
            Padding(
              padding: const EdgeInsets.only(bottom: 10),
              child: Text(label!, style: text.bodyMedium?.copyWith(color: scheme.onSurface)),
            ),
          Wrap(
            spacing: 10,
            runSpacing: 10,
            children: colors.map((c) {
              final isSelected = c == value;
              return GestureDetector(
                onTap: () => onChanged(c),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 150),
                  width: dotSize,
                  height: dotSize,
                  decoration: BoxDecoration(
                    color: c,
                    shape: BoxShape.circle,
                    border: Border.all(
                      color: isSelected ? scheme.onSurface : Colors.transparent,
                      width: 2.5,
                    ),
                    boxShadow: isSelected
                        ? [BoxShadow(color: c.withAlpha(100), blurRadius: 6, spreadRadius: 1)]
                        : null,
                  ),
                  child: isSelected
                      ? Icon(Icons.check_rounded,
                          size: dotSize * 0.5,
                          color: _onColor(c))
                      : null,
                ),
              );
            }).toList(),
          ),
        ],
      ),
    );
  }

  Color _onColor(Color c) {
    final luminance = c.computeLuminance();
    return luminance > 0.4 ? Colors.black : Colors.white;
  }
}
