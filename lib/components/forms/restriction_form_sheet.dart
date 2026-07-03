import 'package:flutter/material.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import '../basic/quick_pick_chip.dart';
import '../basic/sheet_handle.dart';
import '../basic/text_field.dart';
import 'dotted_slider.dart';

/// Hoja inferior para crear una restricción de app o de grupo de apps.
Future<void> showAddRestrictionSheet(
  BuildContext context, {
  required void Function(TechRestriction restriction) onCreate,
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

Future<List<int>?> _showAppPicker(BuildContext context, {required bool multi}) {
  final apps = AppData.mockInstallableApps;
  final selected = <int>{};
  return showModalBottomSheet<List<int>>(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (context) {
      return StatefulBuilder(
        builder: (context, setState) {
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
                      multi ? l10n.restrictionPickApps : l10n.restrictionPickApp,
                      style: Theme.of(context).textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w700),
                    ),
                  ),
                  ...List.generate(apps.length, (i) {
                    final (name, icon, color) = apps[i];
                    final isSelected = selected.contains(i);
                    return ListTile(
                      leading: CircleAvatar(backgroundColor: color.withAlpha(40), child: Icon(icon, color: color)),
                      title: Text(name),
                      trailing: multi ? Checkbox(value: isSelected, onChanged: (_) {
                        setState(() => isSelected ? selected.remove(i) : selected.add(i));
                      }) : null,
                      onTap: () {
                        if (multi) {
                          setState(() => isSelected ? selected.remove(i) : selected.add(i));
                        } else {
                          Navigator.pop(context, [i]);
                        }
                      },
                    );
                  }),
                  if (multi) ...[
                    const SizedBox(height: 8),
                    SizedBox(
                      width: double.infinity,
                      child: FilledButton(
                        onPressed: selected.isEmpty ? null : () => Navigator.pop(context, selected.toList()),
                        child: Text(l10n.restrictionDone),
                      ),
                    ),
                  ],
                ],
              ),
            ),
          );
        },
      );
    },
  );
}

class _RestrictionFormSheet extends StatefulWidget {
  final void Function(TechRestriction restriction) onCreate;
  const _RestrictionFormSheet({required this.onCreate});

  @override
  State<_RestrictionFormSheet> createState() => _RestrictionFormSheetState();
}

class _RestrictionFormSheetState extends State<_RestrictionFormSheet> {
  RestrictionType _type = RestrictionType.app;
  final _groupNameCtrl = TextEditingController();
  int? _selectedAppIndex;
  final List<int> _selectedGroupApps = [];
  int _limitMinutes = 60;

  bool get _appRange => _type == RestrictionType.app;
  double get _min => _appRange ? 30 : 90;
  double get _max => _appRange ? 360 : 600;
  List<int> get _quickOptions => _appRange ? [30, 60, 120, 360] : [90, 120, 240, 600];

  bool get _canCreate => _type == RestrictionType.app
      ? _selectedAppIndex != null
      : _groupNameCtrl.text.trim().isNotEmpty && _selectedGroupApps.isNotEmpty;

  String _fmt(int minutes) {
    if (minutes < 60) return '${minutes}m';
    final h = minutes / 60;
    return '${h % 1 == 0 ? h.toStringAsFixed(0) : h.toStringAsFixed(1)}h';
  }

  void _submit() {
    if (!_canCreate) return;
    final apps = AppData.mockInstallableApps;
    if (_type == RestrictionType.app) {
      final (name, icon, color) = apps[_selectedAppIndex!];
      widget.onCreate(TechRestriction(
        id: DateTime.now().microsecondsSinceEpoch.toString(),
        name: name,
        identifier: 'com.demo.${name.toLowerCase()}',
        icon: icon,
        color: color,
        usedMinutesToday: 0,
        limitMinutes: _limitMinutes,
      ));
    } else {
      widget.onCreate(TechRestriction(
        id: DateTime.now().microsecondsSinceEpoch.toString(),
        name: _groupNameCtrl.text.trim(),
        identifier: '${_selectedGroupApps.length} apps',
        icon: Icons.folder_rounded,
        color: Theme.of(context).colorScheme.primary,
        type: RestrictionType.group,
        usedMinutesToday: 0,
        limitMinutes: _limitMinutes,
      ));
    }
    Navigator.pop(context);
  }

  @override
  void dispose() {
    _groupNameCtrl.dispose();
    super.dispose();
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
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: QuickPickChip(
                      label: l10n.techRestrictionTypeApp,
                      selected: _type == RestrictionType.app,
                      onTap: () => setState(() {
                        _type = RestrictionType.app;
                        _limitMinutes = 60;
                      }),
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: QuickPickChip(
                      label: l10n.techRestrictionTypeGroup,
                      selected: _type == RestrictionType.group,
                      onTap: () => setState(() {
                        _type = RestrictionType.group;
                        _limitMinutes = 90;
                      }),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 20),
              if (_type == RestrictionType.app) ...[
                Text(l10n.restrictionApplication, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
                const SizedBox(height: 8),
                OutlinedButton.icon(
                  onPressed: () async {
                    final result = await _showAppPicker(context, multi: false);
                    if (result != null) setState(() => _selectedAppIndex = result.first);
                  },
                  icon: const Icon(Icons.smartphone_rounded, size: 18),
                  label: Text(_selectedAppIndex == null ? l10n.restrictionPickApp : apps[_selectedAppIndex!].$1),
                  style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                ),
              ] else ...[
                DayPilotTextField(
                  controller: _groupNameCtrl,
                  label: l10n.restrictionGroupName,
                  onChanged: (_) => setState(() {}),
                ),
                const SizedBox(height: 16),
                Text(l10n.restrictionGroupApps, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
                const SizedBox(height: 8),
                OutlinedButton.icon(
                  onPressed: () async {
                    final result = await _showAppPicker(context, multi: true);
                    if (result != null) {
                      setState(() {
                        _selectedGroupApps
                          ..clear()
                          ..addAll(result);
                      });
                    }
                  },
                  icon: const Icon(Icons.add_rounded, size: 18),
                  label: Text(_selectedGroupApps.isEmpty
                      ? l10n.restrictionPickApps
                      : l10n.restrictionAppsSelected(_selectedGroupApps.length)),
                  style: OutlinedButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                ),
              ],
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
                      child: Text(l10n.commonCreate),
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
