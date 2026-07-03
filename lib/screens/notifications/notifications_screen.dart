import 'package:flutter/material.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/notification_card.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class NotificationsScreen extends StatefulWidget {
  const NotificationsScreen({super.key});

  @override
  State<NotificationsScreen> createState() => _NotificationsScreenState();
}

class _NotificationsScreenState extends State<NotificationsScreen> {
  NotificationType? _filter;

  static const _items = AppData.notifications;

  List<AppNotification> get _filtered =>
      _filter == null ? _items : _items.where((n) => n.type == _filter).toList();

  @override
  Widget build(BuildContext context) {
    final items = _filtered;
    final l10n = AppLocalizations.of(context);

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: l10n.navAvisos,
        actions: [
          TextButton(
            onPressed: () {},
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
            child: ListView.builder(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 32),
              itemCount: items.length,
              itemBuilder: (ctx, i) {
                final n = items[i];
                return Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: NotificationCard(
                    type: n.type,
                    content: n.content,
                    timestamp: n.time,
                    read: n.read,
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
