import 'package:flutter/material.dart';
import '../../l10n/app_localizations.dart';
import '../basic/quick_pick_chip.dart';
import '../basic/sheet_handle.dart';
import 'dotted_slider.dart';

Future<void> showStepsGoalSheet(
  BuildContext context, {
  required int currentGoal,
  required ValueChanged<int> onSave,
}) {
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    shape: const RoundedRectangleBorder(
      borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
    ),
    builder: (_) => _StepsGoalSheet(currentGoal: currentGoal, onSave: onSave),
  );
}

class _StepsGoalSheet extends StatefulWidget {
  final int currentGoal;
  final ValueChanged<int> onSave;

  const _StepsGoalSheet({required this.currentGoal, required this.onSave});

  @override
  State<_StepsGoalSheet> createState() => _StepsGoalSheetState();
}

class _StepsGoalSheetState extends State<_StepsGoalSheet> {
  static const _quickGoals = [2000, 5000, 8000, 10000, 15000];
  late int _goal = widget.currentGoal;

  @override
  Widget build(BuildContext context) {
    final colors = Theme.of(context).colorScheme;
    final text = Theme.of(context).textTheme;
    final l10n = AppLocalizations.of(context);

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(20, 12, 20, 20),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const DayPilotSheetHandle(),
            const SizedBox(height: 20),
            Text(
              l10n.stepsGoalTitle,
              style: text.titleLarge?.copyWith(fontWeight: FontWeight.w700),
            ),
            const SizedBox(height: 24),
            Center(
              child: Text(
                l10n.stepsGoalValue(_goal),
                style: text.headlineMedium?.copyWith(
                  color: colors.primary,
                  fontWeight: FontWeight.w700,
                ),
              ),
            ),
            const SizedBox(height: 16),
            DottedSliderField(
              value: _goal.toDouble(),
              min: 1000,
              max: 30000,
              leftLabel: '1.000',
              rightLabel: '30.000',
              onChanged: (v) => setState(() => _goal = (v / 500).round() * 500),
            ),
            const SizedBox(height: 20),
            Text(l10n.stepsGoalQuickGoals, style: text.bodyMedium?.copyWith(color: colors.onSurfaceVariant)),
            const SizedBox(height: 10),
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _quickGoals.map((g) {
                return QuickPickChip(
                  label: g >= 1000 ? '${g ~/ 1000}k' : '$g',
                  selected: _goal == g,
                  onTap: () => setState(() => _goal = g),
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
                    onPressed: () {
                      widget.onSave(_goal);
                      Navigator.pop(context);
                    },
                    child: Text(l10n.commonSave),
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
