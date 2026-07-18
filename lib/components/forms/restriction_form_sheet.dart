import 'package:flutter/material.dart';
import '../../core/logging/app_logger.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import '../basic/quick_pick_chip.dart';
import '../basic/sheet_handle.dart';
import 'dotted_slider.dart';

Future<void> showAddRestrictionSheet(
  BuildContext context, {
  required Future<void> Function({
    required String appPackage,
    required String appName,
    required int limitMinutes,
  }) onCreate,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => _RestrictionFormSheet(onCreate: onCreate),
  );
}

Future<int?> _showAppPicker(BuildContext context) {
  final apps = AppData.mockInstallableApps;
  return showModalBottomSheet<int>(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (context) {
      final l10n = AppLocalizations.of(context);
      return SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(12, 12, 12, 12),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                child: Text(
                  l10n.restrictionPickApp,
                  style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                ),
              ),
              ...List.generate(apps.length, (i) {
                final (name, icon, color) = apps[i];
                return ListTile(
                  leading: CircleAvatar(backgroundColor: color.withAlpha(40), child: Icon(icon, color: color)),
                  title: Text(name),
                  onTap: () => Navigator.pop(context, i),
                );
              }),
            ],
          ),
        ),
      );
    },
  );
}

class _RestrictionFormSheet extends StatefulWidget {
  final Future<void> Function({
    required String appPackage,
    required String appName,
    required int limitMinutes,
  }) onCreate;
  const _RestrictionFormSheet({required this.onCreate});

  @override
  State<_RestrictionFormSheet> createState() => _RestrictionFormSheetState();
}

class _RestrictionFormSheetState extends State<_RestrictionFormSheet> {
  int? _selectedAppIndex;
  int _limitMinutes = 60;
  bool _saving = false;

  static const _min = 30.0;
  static const _max = 360.0;
  static const _quickOptions = [30, 60, 120, 360];

  bool get _canCreate => _selectedAppIndex != null && !_saving;

  String _fmt(int minutes) {
    if (minutes < 60) return '${minutes}m';
    final h = minutes / 60;
    return '${h % 1 == 0 ? h.toStringAsFixed(0) : h.toStringAsFixed(1)}h';
  }

  Future<void> _submit() async {
    if (!_canCreate) return;
    final apps = AppData.mockInstallableApps;
    final (name, _, _) = apps[_selectedAppIndex!];
    setState(() => _saving = true);
    try {
      await widget.onCreate(
        appPackage: 'com.demo.${name.toLowerCase().replaceAll(' ', '')}',
        appName: name,
        limitMinutes: _limitMinutes,
      );
      if (!mounted) return;
      Navigator.pop(context);
    } catch (e, st) {
      AppLogger.logError('RestrictionFormSheet._submit', e, st);
      if (!mounted) return;
      setState(() => _saving = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final apps = AppData.mockInstallableApps;

    return Padding(
      padding: EdgeInsets.only(bottom: MediaQuery.of(context).viewInsets.bottom),
      child: SafeArea(
        child: Padding(
          padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const DayPilotSheetHandle(),
              const SizedBox(height: 20),
              Text(l10n.restrictionNewTitle, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700)),
              const SizedBox(height: 20),
              Text(l10n.restrictionApplication, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () async {
                  final result = await _showAppPicker(context);
                  if (result != null) setState(() => _selectedAppIndex = result);
                },
                icon: const Icon(Icons.smartphone_rounded, size: 18),
                label: Text(_selectedAppIndex == null ? l10n.restrictionPickApp : apps[_selectedAppIndex!].$1),
                style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
              ),
              const SizedBox(height: 20),
              Text(
                l10n.restrictionDailyLimitRange(_fmt(_min.toInt()), _fmt(_max.toInt())),
                style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant),
              ),
              const SizedBox(height: 4),
              Text(
                l10n.restrictionSelected(_fmt(_limitMinutes)),
                style: text.titleSmall?.copyWith(color: colors.primary, fontWeight: FontWeight.w700),
              ),
              const SizedBox(height: 8),
              DottedSliderField(
                value: _limitMinutes.toDouble(),
                min: _min,
                max: _max,
                onChanged: (v) => setState(() => _limitMinutes = v.round()),
              ),
              const SizedBox(height: 16),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _quickOptions.map((m) {
                  return QuickPickChip(
                    label: _fmt(m),
                    selected: _limitMinutes == m,
                    onTap: () => setState(() => _limitMinutes = m),
                  );
                }).toList(),
              ),
              const SizedBox(height: 24),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton(
                      onPressed: () => Navigator.pop(context),
                      child: Text(l10n.commonCancel),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: FilledButton(
                      onPressed: _canCreate ? _submit : null,
                      child: _saving
                          ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                          : Text(l10n.commonCreate),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}
