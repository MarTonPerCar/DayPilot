import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../components/basic/top_bar.dart';
import '../../components/cards/tech_restriction_card.dart';
import '../../components/forms/restriction_form_sheet.dart';
import '../../core/data/models/app_tech_restriction.dart';
import '../../data/app_data.dart';
import '../../features/techhealth/tech_health_notifier.dart';
import '../../l10n/app_localizations.dart';
import 'permissions_screen.dart';

class TechHealthScreen extends ConsumerWidget {
  const TechHealthScreen({super.key});

  static const _genericIcon = Icons.apps_rounded;

  (IconData, Color) _lookAndFeelFor(String appName, ColorScheme colors) {
    for (final (name, icon, color) in AppData.mockInstallableApps) {
      if (name.toLowerCase() == appName.toLowerCase()) return (icon, color);
    }
    return (_genericIcon, colors.onSurfaceVariant);
  }

  TechRestriction _toDisplay(AppTechRestriction r, ColorScheme colors) {
    final (icon, color) = _lookAndFeelFor(r.appName, colors);
    return TechRestriction(
      id: r.appPackage,
      name: r.appName,
      identifier: r.appPackage,
      icon: icon,
      color: color,
      usedMinutesToday: 0,
      limitMinutes: r.limitMinutes,
      enabled: r.isActive,
    );
  }

  void _showPointInfo(BuildContext context, bool pointEarnedToday) {
    final l10n = AppLocalizations.of(context);
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: Text(l10n.techHealthPointDialogTitle),
        content: Text(pointEarnedToday ? l10n.techHealthPointDialogBody : l10n.techHealthPointLostBody),
        actions: [
          TextButton(onPressed: () => Navigator.pop(context), child: Text(l10n.commonAccept)),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final state = ref.watch(techHealthNotifierProvider);
    final restrictions = state.restrictions;

    return Scaffold(
      appBar: DayPilotTopBarWithActions(
        title: l10n.techHealthTitle,
        showBack: true,
        actions: [
          IconButton(
            icon: const Icon(Icons.lock_outline_rounded),
            tooltip: l10n.permissionsTitle,
            onPressed: () => Navigator.push(
              context,
              MaterialPageRoute(builder: (_) => const PermissionsScreen()),
            ),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 100),
        children: [
          GestureDetector(
            onTap: () => _showPointInfo(context, state.pointEarnedToday),
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
              decoration: BoxDecoration(
                color: state.pointEarnedToday ? colors.primaryContainer : colors.errorContainer,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Row(
                children: [
                  CircleAvatar(
                    radius: 14,
                    backgroundColor: state.pointEarnedToday ? colors.primary : colors.error,
                    child: Icon(
                      state.pointEarnedToday ? Icons.info_outline_rounded : Icons.error_outline_rounded,
                      size: 16,
                      color: state.pointEarnedToday ? colors.onPrimary : colors.onError,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      state.pointEarnedToday ? l10n.techHealthPointBannerLabel : l10n.techHealthPointLostLabel,
                      style: text.bodyMedium?.copyWith(
                        color: state.pointEarnedToday ? colors.onPrimaryContainer : colors.onErrorContainer,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          Text(
            l10n.techHealthRestrictionsTitle,
            style: text.titleMedium?.copyWith(fontWeight: FontWeight.w700),
          ),
          Text(
            l10n.techHealthRestrictionsCount(restrictions.length),
            style: text.bodySmall?.copyWith(color: colors.onSurfaceVariant),
          ),
          const SizedBox(height: 12),
          ...restrictions.map((r) => Padding(
                padding: const EdgeInsets.only(bottom: 12),
                child: TechRestrictionCard(
                  restriction: _toDisplay(r, colors),
                  onToggle: (v) => ref.read(techHealthNotifierProvider.notifier).toggleRestriction(r.appPackage, v),
                  onDelete: () => ref.read(techHealthNotifierProvider.notifier).deleteRestriction(r.appPackage),
                ),
              )),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => showAddRestrictionSheet(
          context,
          onCreate: ({required appPackage, required appName, required limitMinutes}) {
            return ref.read(techHealthNotifierProvider.notifier).saveRestriction(
                  appPackage: appPackage,
                  appName: appName,
                  limitMinutes: limitMinutes,
                );
          },
        ),
        child: const Icon(Icons.add_rounded),
      ),
    );
  }
}
