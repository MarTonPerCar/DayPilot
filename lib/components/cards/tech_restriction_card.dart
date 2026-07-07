import 'package:flutter/material.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';

class TechRestrictionCard extends StatelessWidget {
  final TechRestriction restriction;
  final ValueChanged<bool> onToggle;
  final VoidCallback? onDelete;

  const TechRestrictionCard({
    super.key,
    required this.restriction,
    required this.onToggle,
    this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final ratio = restriction.limitMinutes > 0
        ? (restriction.usedMinutesToday / restriction.limitMinutes).clamp(0.0, 1.0)
        : 0.0;
    final over = restriction.usedMinutesToday > restriction.limitMinutes;
    final barColor = over ? colors.error : restriction.color;

    return Card.filled(
      clipBehavior: Clip.hardEdge,
      margin: EdgeInsets.zero,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          children: [
            Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: restriction.color.withAlpha(35),
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(restriction.icon, color: restriction.color, size: 24),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Flexible(
                            child: Text(
                              restriction.name,
                              style: text.bodyLarge?.copyWith(fontWeight: FontWeight.w700),
                              overflow: TextOverflow.ellipsis,
                            ),
                          ),
                          const SizedBox(width: 6),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: colors.surfaceContainerHigh,
                              borderRadius: BorderRadius.circular(8),
                            ),
                            child: Text(
                              restriction.type == RestrictionType.app
                                  ? l10n.techRestrictionTypeApp
                                  : l10n.techRestrictionTypeGroup,
                              style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant),
                            ),
                          ),
                        ],
                      ),
                      Text(
                        restriction.identifier,
                        style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ],
                  ),
                ),
                Switch(value: restriction.enabled, onChanged: onToggle),
              ],
            ),
            const SizedBox(height: 10),
            ClipRRect(
              borderRadius: BorderRadius.circular(4),
              child: LinearProgressIndicator(
                value: ratio,
                minHeight: 4,
                backgroundColor: colors.surfaceContainerHigh,
                color: barColor,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Flexible(
                  child: Text(
                    l10n.techRestrictionUsageToday(restriction.usedMinutesToday, restriction.limitMinutes),
                    style: text.labelMedium?.copyWith(color: over ? colors.error : colors.onSurfaceVariant),
                    overflow: TextOverflow.ellipsis,
                  ),
                ),
                const SizedBox(width: 8),
                Flexible(
                  child: Text(
                    l10n.techRestrictionNotifyEvery(restriction.notifyIntervalSeconds),
                    style: text.labelMedium?.copyWith(color: colors.onSurfaceVariant),
                    overflow: TextOverflow.ellipsis,
                    textAlign: TextAlign.end,
                  ),
                ),
              ],
            ),
            if (onDelete != null) ...[
              const SizedBox(height: 10),
              SizedBox(
                width: double.infinity,
                child: TextButton(
                  onPressed: onDelete,
                  style: TextButton.styleFrom(
                    backgroundColor: colors.surfaceContainerHigh,
                    foregroundColor: colors.onSurfaceVariant,
                  ),
                  child: Text(l10n.techRestrictionDeletesTomorrow),
                ),
              ),
            ],
          ],
        ),
      ),
    );
  }
}
