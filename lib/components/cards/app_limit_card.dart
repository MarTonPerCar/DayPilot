import 'package:flutter/material.dart';

class AppLimitCard extends StatelessWidget {
  final String appName;
  final IconData appIcon;
  final int usageMinutes;
  final int limitMinutes;
  final bool enabled;
  final VoidCallback? onToggle;

  const AppLimitCard({
    super.key,
    required this.appName,
    this.appIcon = Icons.apps_rounded,
    required this.usageMinutes,
    required this.limitMinutes,
    this.enabled = true,
    this.onToggle,
  });

  Color _progressColor(double ratio, ColorScheme colors) {
    if (ratio >= 1.0) return colors.error;
    if (ratio >= 0.75) return colors.tertiary;
    return colors.primary;
  }

  String _formatMin(int minutes) {
    if (minutes < 60) return '${minutes}m';
    final h = minutes ~/ 60;
    final m = minutes % 60;
    return m == 0 ? '${h}h' : '${h}h ${m}m';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final ratio = limitMinutes > 0 ? (usageMinutes / limitMinutes).clamp(0.0, 1.0) : 0.0;
    final accent = _progressColor(ratio, colors);

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
        child: Column(
          children: [
            Row(
              children: [
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: colors.surfaceContainerHighest,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Icon(appIcon, color: colors.onSurfaceVariant, size: 22),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        appName,
                        style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w500),
                      ),
                      Text(
                        '${_formatMin(usageMinutes)} / ${_formatMin(limitMinutes)}',
                        style: text.bodySmall
                            ?.copyWith(color: colors.onSurfaceVariant),
                      ),
                    ],
                  ),
                ),
                Switch(value: enabled, onChanged: (_) => onToggle?.call()),
              ],
            ),
            const SizedBox(height: 10),
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: ratio,
                minHeight: 6,
                backgroundColor: accent.withAlpha(30),
                color: enabled ? accent : colors.surfaceContainerHighest,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class GroupLimitCard extends StatelessWidget {
  final String groupName;
  final IconData groupIcon;
  final int appCount;
  final int usageMinutes;
  final int limitMinutes;
  final bool enabled;
  final VoidCallback? onTap;
  final VoidCallback? onToggle;

  const GroupLimitCard({
    super.key,
    required this.groupName,
    this.groupIcon = Icons.folder_rounded,
    required this.appCount,
    required this.usageMinutes,
    required this.limitMinutes,
    this.enabled = true,
    this.onTap,
    this.onToggle,
  });

  Color _progressColor(double ratio, ColorScheme colors) {
    if (ratio >= 1.0) return colors.error;
    if (ratio >= 0.75) return colors.tertiary;
    return colors.primary;
  }

  String _formatMin(int minutes) {
    if (minutes < 60) return '${minutes}m';
    final h = minutes ~/ 60;
    final m = minutes % 60;
    return m == 0 ? '${h}h' : '${h}h ${m}m';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final ratio = limitMinutes > 0 ? (usageMinutes / limitMinutes).clamp(0.0, 1.0) : 0.0;
    final accent = _progressColor(ratio, colors);

    return Card(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: InkWell(
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Column(
            children: [
              Row(
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: colors.primaryContainer,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(groupIcon, color: colors.onPrimaryContainer, size: 24),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          groupName,
                          style: text.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                        ),
                        Text(
                          '$appCount apps · ${_formatMin(usageMinutes)} / ${_formatMin(limitMinutes)}',
                          style: text.bodySmall
                              ?.copyWith(color: colors.onSurfaceVariant),
                        ),
                      ],
                    ),
                  ),
                  Switch(value: enabled, onChanged: (_) => onToggle?.call()),
                ],
              ),
              const SizedBox(height: 10),
              ClipRRect(
                borderRadius: BorderRadius.circular(4),
                child: LinearProgressIndicator(
                  value: ratio,
                  minHeight: 8,
                  backgroundColor: accent.withAlpha(30),
                  color: enabled ? accent : colors.surfaceContainerHighest,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
