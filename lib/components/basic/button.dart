import 'package:flutter/material.dart';

enum DayPilotButtonVariant { filled, outline }

class DayPilotButton extends StatelessWidget {
  final String label;
  final VoidCallback? onPressed;
  final DayPilotButtonVariant variant;
  final bool isLoading;
  final IconData? icon;

  const DayPilotButton({
    super.key,
    required this.label,
    this.onPressed,
    this.variant = DayPilotButtonVariant.filled,
    this.isLoading = false,
    this.icon,
  });

  Widget _child(BuildContext context) {
    if (isLoading) {
      return SizedBox(
        width: 20,
        height: 20,
        child: CircularProgressIndicator(
          strokeWidth: 2,
          color: variant == DayPilotButtonVariant.filled
              ? Theme.of(context).colorScheme.onPrimary
              : Theme.of(context).colorScheme.primary,
        ),
      );
    }
    if (icon != null) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: [Icon(icon, size: 18), const SizedBox(width: 8), Text(label)],
      );
    }
    return Text(label);
  }

  @override
  Widget build(BuildContext context) {
    final child = _child(context);
    if (variant == DayPilotButtonVariant.outline) {
      return OutlinedButton(
        onPressed: isLoading ? null : onPressed,
        child: child,
      );
    }
    return FilledButton(
      onPressed: isLoading ? null : onPressed,
      child: child,
    );
  }
}
