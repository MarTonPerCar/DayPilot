import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/notification_card.dart';
import '../../core/data/models/app_notification_item.dart';
import '../../features/notifications/notifications_notifier.dart';
import '../../l10n/app_localizations.dart';

NotificationType _uiType(AppNotificationType type) => switch (type) {
      AppNotificationType.friendRequest => NotificationType.social,
      AppNotificationType.friendAccepted => NotificationType.social,
      AppNotificationType.reaction => NotificationType.social,
      AppNotificationType.levelUp => NotificationType.achievement,
      AppNotificationType.streakRisk => NotificationType.streak,
      AppNotificationType.stepsGoal => NotificationType.steps,
      AppNotificationType.taskCompleted => NotificationType.task,
      AppNotificationType.timerDone => NotificationType.task,
      AppNotificationType.taskReminder => NotificationType.reminder,
      AppNotificationType.dailySummary => NotificationType.reminder,
    };

String _relativeTime(AppLocalizations l10n, DateTime createdAt) {
  final diff = DateTime.now().difference(createdAt);
  if (diff.inMinutes < 1) return l10n.notifTimeJustNow;
  if (diff.inMinutes < 60) return l10n.notifTimeMinutesAgo(diff.inMinutes);
  if (diff.inHours < 24) return l10n.notifTimeHoursAgo(diff.inHours);
  if (diff.inDays == 1) return l10n.notifTimeYesterday;
  return l10n.notifTimeDaysAgo(diff.inDays);
}

class NotificationsScreen extends ConsumerStatefulWidget {
  const NotificationsScreen({super.key});

  @override
  ConsumerState<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends ConsumerState<NotificationsScreen> {
  NotificationType? _filter;

  @override
  Widget build(BuildContext context) {
    final l10n = AppLocalizations.of(context);
    final items = ref.watch(notificationsNotifierProvider);
    final filtered = _filter == null ? items : items.where((n) => _uiType(n.type) == _filter).toList();

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: l10n.navAvisos,
        actions: [
          TextButton(
            onPressed: () => ref.read(notificationsNotifierProvider.notifier).markAllAsRead(),
            child: Text(l10n.notificationsMarkAllRead),
          ),
        ],
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 8, 16, 0),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _FilterChip(
                    label: l10n.calendarAll,
                    selected: _filter == null,
                    onTap: () => setState(() => _filter = null),
                  ),
                  ...NotificationType.values.map((t) => _FilterChip(
                        emoji: t.emoji,
                        label: t.label(context),
                        color: t.color(Theme.of(context).colorScheme),
                        selected: _filter == t,
                        onTap: () => setState(() => _filter = t),
                      )),
                ],
              ),
            ),
          ),
          Expanded(
            child: filtered.isEmpty
                ? Center(child: Text(l10n.notificationsEmpty))
                : ListView.builder(
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
                    itemCount: filtered.length,
                    itemBuilder: (ctx, i) {
                      final n = filtered[i];
                      return Padding(
                        padding: const EdgeInsets.only(bottom: 8),
                        child: NotificationCard(
                          type: _uiType(n.type),
                          content: n.body,
                          timestamp: _relativeTime(l10n, n.createdAt),
                          read: n.isRead,
                          onTap: n.isRead
                              ? null
                              : () => ref.read(notificationsNotifierProvider.notifier).markAsRead(n.id),
                        ),
                      );
                    },
                  ),
          ),
        ],
      ),
    );
  }
}

class _FilterChip extends StatelessWidget {
  final String? emoji;
  final String label;
  final Color? color;
  final bool selected;
  final VoidCallback onTap;

  const _FilterChip({
    this.emoji,
    required this.label,
    this.color,
    required this.selected,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final tint = color ?? colors.onSurfaceVariant;

    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: onTap,
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 150),
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          decoration: BoxDecoration(
            color: selected ? tint : tint.withAlpha(25),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: tint.withAlpha(selected ? 255 : 90)),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              if (emoji != null) ...[
                Text(emoji!, style: const TextStyle(fontSize: 14)),
                const SizedBox(width: 6),
              ],
              Text(
                label,
                style: text.labelMedium?.copyWith(
                  fontWeight: FontWeight.w600,
                  color: selected ? _onColor(tint) : tint,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Color _onColor(Color c) => c.computeLuminance() > 0.5 ? Colors.black : Colors.white;
}
