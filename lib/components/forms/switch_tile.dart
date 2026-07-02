import 'package:flutter/material.dart';

class DayPilotSwitchTile extends StatelessWidget {
  final String label;
  final String? subtitle;
  final bool value;
  final ValueChanged<bool>? onChanged;
  final IconData? icon;

  const DayPilotSwitchTile({
    super.key,
    required this.label,
    this.subtitle,
    required this.value,
    this.onChanged,
    this.icon,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    return SwitchListTile(
      title: Text(label),
      subtitle: subtitle != null ? Text(subtitle!) : null,
      secondary: icon != null
          ? Icon(icon, color: colors.onSurfaceVariant)
          : null,
      value: value,
      onChanged: onChanged,
      contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
    );
  }
}
