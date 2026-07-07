import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../l10n/app_localizations.dart';

class ReminderCard extends StatelessWidget {
  final String title;
  final DateTime dateTime;
  final bool enabled;
  final ValueChanged<bool> onToggle;
  final VoidCallback onDelete;

  const ReminderCard({
    super.key,
    required this.title,
    required this.dateTime,
    required this.enabled,
    required this.onToggle,
    required this.onDelete,
  });

  static String formatWhen(AppLocalizations l10n, String locale, DateTime dt, DateTime now) {
    final time = '${dt.hour.toString().padLeft(2, '0')}:${dt.minute.toString().padLeft(2, '0')}';
    final today = DateTime(now.year, now.month, now.day);
    final target = DateTime(dt.year, dt.month, dt.day);
    final diff = target.difference(today).inDays;
    if (diff == 0) return '${l10n.commonToday}, $time';
    if (diff == 1) return '${l10n.commonTomorrow}, $time';
    final month = DateFormat.MMM(locale).format(dt);
    return '${dt.day} $month, $time';
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final locale = Localizations.localeOf(context).languageCode;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(title, style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w700)),
                  Text(
                    formatWhen(l10n, locale, dateTime, DateTime.now()),
                    style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                  ),
                ],
              ),
            ),
            IconButton(
              icon: Icon(Icons.delete_outline_rounded, color: colors.error),
              onPressed: onDelete,
            ),
            Switch(value: enabled, onChanged: onToggle),
          ],
        ),
      ),
    );
  }
}
