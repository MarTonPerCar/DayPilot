import 'package:flutter/material.dart';

enum NotificationType { friendRequest, reaction, levelUp, dailySummary, streakAlert }

class NotificationCard extends StatelessWidget {
  final NotificationType type;
  final String content;
  final String timestamp;
  final bool read;
  final VoidCallback? onTap;

  const NotificationCard({
    super.key,
    required this.type,
    required this.content,
    required this.timestamp,
    this.read = false,
    this.onTap,
  });

  (IconData, Color) _typeStyle(ColorScheme colors) => switch (type) {
        NotificationType.friendRequest => (Icons.person_add_rounded, colors.primary),
        NotificationType.reaction      => (Icons.emoji_emotions_rounded, colors.tertiary),
        NotificationType.levelUp       => (Icons.trending_up_rounded, const Color(0xFFFFD700)),
        NotificationType.dailySummary  => (Icons.bar_chart_rounded, colors.secondary),
        NotificationType.streakAlert   => (Icons.local_fire_department_rounded, colors.error),
      };

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final (icon, iconColor) = _typeStyle(colors);

    return Material(
      color: read ? colors.surfaceContainerLow : colors.primaryContainer.withAlpha(60),
      borderRadius: BorderRadius.circular(12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
          child: Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: iconColor.withAlpha(30),
                  shape: BoxShape.circle,
                ),
                child: Icon(icon, color: iconColor, size: 22),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      content,
                      style: text.bodyMedium?.copyWith(
                        color: colors.onSurface,
                        fontWeight: read ? FontWeight.w400 : FontWeight.w500,
                      ),
                    ),
                    const SizedBox(height: 2),
                    Text(
                      timestamp,
                      style: text.labelSmall
                          ?.copyWith(color: colors.onSurfaceVariant),
                    ),
                  ],
                ),
              ),
              if (!read)
                Container(
                  width: 8,
                  height: 8,
                  margin: const EdgeInsets.only(left: 8),
                  decoration: BoxDecoration(
                    color: colors.primary,
                    shape: BoxShape.circle,
                  ),
                ),
            ],
          ),
        ),
      ),
    );
  }
}
