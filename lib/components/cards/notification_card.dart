import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';

enum NotificationType { task, social, steps, streak, reminder, achievement }

extension NotificationTypeLabel on NotificationType {
  String label(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    return switch (this) {
      NotificationType.task => l10n.commonTasks,
      NotificationType.social => l10n.notifTypeSocial,
      NotificationType.steps => l10n.commonSteps,
      NotificationType.streak => l10n.notifTypeStreak,
      NotificationType.reminder => l10n.notifTypeReminder,
      NotificationType.achievement => l10n.notifTypeAchievement,
    };
  }

  String get emoji => switch (this) {
        NotificationType.task        => '✅',
        NotificationType.social      => '👥',
        NotificationType.steps       => '👣',
        NotificationType.streak      => '🔥',
        NotificationType.reminder    => '🔔',
        NotificationType.achievement => '🏆',
      };

  IconData get icon => switch (this) {
        NotificationType.task        => Icons.check_circle_rounded,
        NotificationType.social      => Icons.people_rounded,
        NotificationType.steps       => Icons.directions_walk_rounded,
        NotificationType.streak      => Icons.local_fire_department_rounded,
        NotificationType.reminder    => Icons.notifications_rounded,
        NotificationType.achievement => Icons.emoji_events_rounded,
      };

  Color color(ColorScheme colors) => switch (this) {
        NotificationType.task        => colors.primary,
        NotificationType.social      => colors.secondary,
        NotificationType.steps       => colors.tertiary,
        NotificationType.streak      => colors.error,
        NotificationType.reminder    => const Color(0xFF9C27B0),
        NotificationType.achievement => const Color(0xFFFFD700),
      };
}

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

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final icon = type.icon;
    final iconColor = type.color(colors);

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
