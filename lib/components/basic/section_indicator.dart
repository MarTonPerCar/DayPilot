import 'package:flutter/material.dart';

class HomeSectionIndicator extends StatelessWidget {
  final String label;
  final IconData icon;
  final bool isActive;

  const HomeSectionIndicator({
    super.key,
    required this.label,
    required this.icon,
    this.isActive = false,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text   = Theme.of(context).textTheme;

    return AnimatedContainer(
      duration: const Duration(milliseconds: 200),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
      decoration: BoxDecoration(
        color: isActive ? colors.primaryContainer : colors.surfaceContainerHighest,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(
            icon,
            size: 16,
            color: isActive ? colors.onPrimaryContainer : colors.onSurfaceVariant,
          ),
          const SizedBox(width: 6),
          Text(
            label,
            style: text.labelMedium?.copyWith(
              color: isActive ? colors.onPrimaryContainer : colors.onSurfaceVariant,
              fontWeight: isActive ? FontWeight.w600 : FontWeight.w400,
            ),
          ),
        ],
      ),
    );
  }
}
