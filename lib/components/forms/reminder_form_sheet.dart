import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import '../../data/app_data.dart';
import '../../l10n/app_localizations.dart';
import '../basic/quick_pick_chip.dart';
import '../basic/sheet_handle.dart';
import '../basic/text_field.dart';
import 'switch_tile.dart';

/// Hoja inferior para crear un nuevo recordatorio.
Future<void> showReminderFormSheet(
  BuildContext context, {
  required void Function(AppReminder reminder) onSave,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => _ReminderFormSheet(onSave: onSave),
  );
}

class _ReminderFormSheet extends StatefulWidget {
  final void Function(AppReminder reminder) onSave;
  const _ReminderFormSheet({required this.onSave});

  @override
  State<_ReminderFormSheet> createState() => _ReminderFormSheetState();
}

class _ReminderFormSheetState extends State<_ReminderFormSheet> {
  final _titleCtrl = TextEditingController();
  static const _quickDurations = [
    (label: '5m', minutes: 5),
    (label: '10m', minutes: 10),
    (label: '15m', minutes: 15),
    (label: '30m', minutes: 30),
    (label: '1h', minutes: 60),
  ];
  static const _frequencyKeys = ['once', 'daily', 'weekly'];

  DateTime? _pickedDateTime;
  int? _selectedQuickMinutes;
  String _frequency = 'once';
  bool _notifyBefore = false;

  bool get _canCreate => _titleCtrl.text.trim().isNotEmpty && _pickedDateTime != null;

  Future<void> _pickDateTime() async {
    final now = DateTime.now();
    final date = await showDatePicker(
      context: context,
      initialDate: _pickedDateTime ?? now,
      firstDate: now.subtract(const Duration(days: 1)),
      lastDate: DateTime(now.year + 2),
    );
    if (date == null || !mounted) return;
    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_pickedDateTime ?? now),
    );
    if (time == null || !mounted) return;
    setState(() {
      _pickedDateTime = DateTime(date.year, date.month, date.day, time.hour, time.minute);
      _selectedQuickMinutes = null;
    });
  }

  String _formatPicked(BuildContext context, DateTime dt) {
    final h = dt.hour.toString().padLeft(2, '0');
    final m = dt.minute.toString().padLeft(2, '0');
    final month = DateFormat.MMM(Localizations.localeOf(context).languageCode).format(dt);
    return '${dt.day} $month, $h:$m';
  }

  void _submit() {
    if (!_canCreate) return;
    widget.onSave(AppReminder(
      id: DateTime.now().microsecondsSinceEpoch.toString(),
      title: _titleCtrl.text.trim(),
      dateTime: _pickedDateTime!,
      frequency: _frequency,
      notifyBefore: _notifyBefore,
    ));
    Navigator.pop(context);
  }

  @override
  void dispose() {
    _titleCtrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);
    final frequencyLabels = {
      'once': l10n.reminderFrequencyOnce,
      'daily': l10n.reminderFrequencyDaily,
      'weekly': l10n.reminderFrequencyWeekly,
    };

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
              Text(l10n.reminderNewTitle, style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700)),
              const SizedBox(height: 16),
              DayPilotTextField(
                controller: _titleCtrl,
                label: l10n.reminderNameLabel,
                onChanged: (_) => setState(() {}),
              ),
              const SizedBox(height: 20),
              Text(l10n.reminderQuickAccess, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
              const SizedBox(height: 10),
              Wrap(
                spacing: 8,
                runSpacing: 8,
                children: _quickDurations.map((q) {
                  return QuickPickChip(
                    label: q.label,
                    selected: _selectedQuickMinutes == q.minutes,
                    onTap: () => setState(() {
                      _selectedQuickMinutes = q.minutes;
                      _pickedDateTime = DateTime.now().add(Duration(minutes: q.minutes));
                    }),
                  );
                }).toList(),
              ),
              const SizedBox(height: 16),
              Row(
                children: [
                  Expanded(child: Divider(color: colors.outlineVariant)),
                  Padding(
                    padding: const EdgeInsets.symmetric(horizontal: 10),
                    child: Text(l10n.reminderOrPickDateTime, style: text.labelSmall?.copyWith(color: colors.onSurfaceVariant)),
                  ),
                  Expanded(child: Divider(color: colors.outlineVariant)),
                ],
              ),
              const SizedBox(height: 16),
              OutlinedButton.icon(
                onPressed: _pickDateTime,
                icon: const Icon(Icons.calendar_today_outlined, size: 18),
                label: Text(_pickedDateTime == null
                    ? l10n.reminderNoDateSelected
                    : _formatPicked(context, _pickedDateTime!)),
                style: OutlinedButton.styleFrom(
                  minimumSize: const Size.fromHeight(48),
                  alignment: Alignment.centerLeft,
                ),
              ),
              const SizedBox(height: 20),
              Text(l10n.reminderFrequency, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
              const SizedBox(height: 10),
              Row(
                children: _frequencyKeys.map((f) {
                  return Expanded(
                    child: Padding(
                      padding: const EdgeInsets.only(right: 8),
                      child: QuickPickChip(
                        label: frequencyLabels[f]!,
                        selected: _frequency == f,
                        onTap: () => setState(() => _frequency = f),
                      ),
                    ),
                  );
                }).toList(),
              ),
              const SizedBox(height: 12),
              Card.filled(
                clipBehavior: Clip.hardEdge,
                margin: EdgeInsets.zero,
                child: DayPilotSwitchTile(
                  label: l10n.reminderNotifyBefore,
                  subtitle: l10n.reminderNotifyBeforeSubtitle,
                  value: _notifyBefore,
                  onChanged: (v) => setState(() => _notifyBefore = v),
                ),
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
                      child: Text(l10n.reminderCreateButton),
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
